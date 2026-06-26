package vn.elca.training.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import vn.elca.training.mapper.ProjectMapper;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.EmployeeService;
import vn.elca.training.service.GroupService;
import vn.elca.training.validator.ProjectValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectServiceImpl Unit Tests")
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ProjectValidator projectValidator;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Group group;
    private Employee employeeQmv;
    private Employee employeeHnh;
    private Project project;
    private ProjectDto projectDto;
    private ProjectDto projectSummaryDto;

    @BeforeEach
    void setUp() {
        employeeQmv = new Employee("QMV", "Quy", "Van", LocalDate.of(1990, 2, 3));
        ReflectionTestUtils.setField(employeeQmv, "id", 1L);

        employeeHnh = new Employee("HNH", "Hanh", "Ho", LocalDate.of(1992, 5, 12));
        ReflectionTestUtils.setField(employeeHnh, "id", 2L);

        group = new Group();
        group.setLeader(employeeQmv);
        ReflectionTestUtils.setField(group, "id", 10L);

        project = new Project();
        project.setProjectNumber(3116L);
        project.setName("Facturation");
        project.setCustomer("Les Rataites Populaires");
        project.setStatus(Status.NEW);
        project.setStartDate(LocalDate.of(2004, 2, 25));
        project.assignGroup(group);
        project.getEmployees().add(employeeQmv);
        ReflectionTestUtils.setField(project, "id", 100L);

        projectDto = new ProjectDto(
                100L, 3116L, "Facturation", "Les Rataites Populaires",
                Status.NEW, LocalDate.of(2004, 2, 25), null, null, null
        );
        projectSummaryDto = new ProjectDto(
                100L, 3116L, "Facturation", "Les Rataites Populaires",
                Status.NEW, LocalDate.of(2004, 2, 25), null, null, null
        );
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("findAll should return mapped summaries for provided pageable")
        void findAll_shouldReturnMappedSummaries() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("projectNumber").ascending());
            Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project));
            when(projectRepository.findAll(pageable)).thenReturn(projectPage);
            when(projectMapper.toProjectSummary(project)).thenReturn(projectSummaryDto);

            Page<ProjectDto> result = projectService.findAll(pageable);

            assertThat(result.getContent()).containsExactly(projectSummaryDto);
            verify(projectRepository).findAll(pageable);
            verify(projectMapper).toProjectSummary(project);
        }

        @Test
        @DisplayName("findProjectById should return mapped project DTO when project exists")
        void findProjectById_shouldReturnDto_whenProjectExists() {
            when(projectRepository.findProjectById(100L)).thenReturn(Optional.of(project));
            when(projectMapper.toProjectDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.findProjectById(100L);

            assertThat(result).isEqualTo(projectDto);
            verify(projectRepository).findProjectById(100L);
            verify(projectMapper).toProjectDto(project);
        }

        @Test
        @DisplayName("findProjectsByCriteria should delegate to repository when only status is provided")
        void findProjectsByCriteria_shouldUseRepository_whenOnlyStatusProvided() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("projectNumber").ascending());
            Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project));
            when(projectRepository.findProjectsByCriteria(null, Status.NEW, pageable)).thenReturn(projectPage);
            when(projectMapper.toProjectSummary(project)).thenReturn(projectSummaryDto);

            Page<ProjectDto> result = projectService.findProjectsByCriteria(null, Status.NEW, pageable);

            assertThat(result.getContent()).containsExactly(projectSummaryDto);
            verify(projectRepository).findProjectsByCriteria(null, Status.NEW, pageable);
            verify(projectRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("findProjectsByCriteria should use findAll when no criteria are provided")
        void findProjectsByCriteria_shouldUseFindAll_whenNoCriteriaProvided() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("projectNumber").ascending());
            Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project));
            when(projectRepository.findAll(pageable)).thenReturn(projectPage);
            when(projectMapper.toProjectSummary(project)).thenReturn(projectSummaryDto);

            Page<ProjectDto> result = projectService.findProjectsByCriteria(null, null, pageable);

            assertThat(result.getContent()).containsExactly(projectSummaryDto);
            verify(projectRepository).findAll(pageable);
            verify(projectRepository, never()).findProjectsByCriteria(any(), any(), any(Pageable.class));
        }

        @Test
        @DisplayName("findProjectsByCriteria should return mapped page from repository")
        void findProjectsByCriteria_shouldReturnMappedPage() {
            Pageable pageable = Pageable.unpaged();
            Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project));
            when(projectRepository.findProjectsByCriteria("fact", Status.NEW, pageable)).thenReturn(projectPage);
            when(projectMapper.toProjectSummary(project)).thenReturn(projectSummaryDto);

            Page<ProjectDto> result = projectService.findProjectsByCriteria("fact", Status.NEW, pageable);

            assertThat(result.getContent()).containsExactly(projectSummaryDto);
            verify(projectRepository).findProjectsByCriteria("fact", Status.NEW, pageable);
        }

        @Test
        @DisplayName("createProject should persist project with group and members")
        void createProject_shouldCreateProject_whenRequestIsValid() {
            ProjectCreationRequest request = new ProjectCreationRequest(
                    9001L,
                    "New Project",
                    "New Customer",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    Arrays.asList("QMV", "HNH"),
                    10L
            );

            doNothing().when(projectValidator).validateCreateProject(request);
            when(groupService.findById(10L)).thenReturn(group);
            when(employeeService.findEmployeesByVisas(argThat(visas ->
                    visas.size() == 2 && visas.containsAll(Arrays.asList("QMV", "HNH")))))
                    .thenReturn(Arrays.asList(employeeQmv, employeeHnh));
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
                Project saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 200L);
                return saved;
            });
            when(projectMapper.toProjectSummary(any(Project.class))).thenReturn(projectSummaryDto);

            ProjectDto result = projectService.createProject(request);

            assertThat(result).isEqualTo(projectSummaryDto);

            ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
            verify(projectValidator).validateCreateProject(request);
            verify(groupService).findById(10L);
            verify(employeeService).findEmployeesByVisas(argThat(visas ->
                    visas.size() == 2 && visas.containsAll(Arrays.asList("QMV", "HNH"))));
            verify(projectRepository).save(projectCaptor.capture());

            Project savedProject = projectCaptor.getValue();
            assertThat(savedProject.getProjectNumber()).isEqualTo(9001L);
            assertThat(savedProject.getName()).isEqualTo("New Project");
            assertThat(savedProject.getCustomer()).isEqualTo("New Customer");
            assertThat(savedProject.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(savedProject.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(savedProject.getStatus()).isEqualTo(Status.NEW);
            assertThat(savedProject.getGroup()).isEqualTo(group);
            assertThat(savedProject.getEmployees()).containsExactlyInAnyOrder(employeeQmv, employeeHnh);
        }

        @Test
        @DisplayName("updateProject should update fields, group and members")
        void updateProject_shouldUpdateProject_whenRequestIsValid() {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name",
                    "Updated Customer",
                    Status.INP,
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 11, 30),
                    Arrays.asList("QMV", "HNH"),
                    10L
            );

            when(projectValidator.validateUpdateProject(100L, request)).thenReturn(project);
            when(groupService.findById(10L)).thenReturn(group);
            when(employeeService.findEmployeesByVisas(Collections.singletonList("HNH")))
                    .thenReturn(Collections.singletonList(employeeHnh));
            when(projectMapper.toProjectDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.updateProject(100L, request);

            assertThat(result).isEqualTo(projectDto);
            assertThat(project.getName()).isEqualTo("Updated Name");
            assertThat(project.getCustomer()).isEqualTo("Updated Customer");
            assertThat(project.getStatus()).isEqualTo(Status.INP);
            assertThat(project.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(project.getEndDate()).isEqualTo(LocalDate.of(2026, 11, 30));
            assertThat(project.getProjectNumber()).isEqualTo(3116L);
            assertThat(project.getEmployees()).containsExactlyInAnyOrder(employeeQmv, employeeHnh);
            assertThat(project.getGroup()).isEqualTo(group);

            verify(projectValidator).validateUpdateProject(100L, request);
            verify(groupService).findById(10L);
            verify(employeeService).findEmployeesByVisas(Collections.singletonList("HNH"));
            verify(projectMapper).toProjectDto(project);
        }

        @Test
        @DisplayName("deleteProject should delete project when status is NEW")
        void deleteProject_shouldDeleteProject_whenProjectIsDeletable() {
            when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
            doNothing().when(projectValidator).validateDeleteProject(project);

            projectService.deleteProject(100L);

            verify(projectRepository).findById(100L);
            verify(projectValidator).validateDeleteProject(project);
            verify(projectRepository).delete(project);
        }

        @Test
        @DisplayName("deleteProjects should delete all requested NEW projects")
        void deleteProjects_shouldDeleteProjects_whenAllAreDeletable() {
            Project secondProject = new Project();
            secondProject.setProjectNumber(3118L);
            secondProject.setStatus(Status.NEW);
            ReflectionTestUtils.setField(secondProject, "id", 101L);

            List<Long> requestedIds = Arrays.asList(100L, 101L);
            List<Project> projects = Arrays.asList(project, secondProject);

            when(projectRepository.findAllById(requestedIds)).thenReturn(projects);
            doNothing().when(projectValidator).validateDeleteProjects(projects, requestedIds);

            projectService.deleteProjects(requestedIds);

            verify(projectRepository).findAllById(requestedIds);
            verify(projectValidator).validateDeleteProjects(projects, requestedIds);
            verify(projectRepository).deleteAll(projects);
        }
    }

    @Nested
    @DisplayName("Exception and edge cases")
    class ExceptionAndEdgeCases {

        @Test
        @DisplayName("findProjectById should throw when project does not exist")
        void findProjectById_shouldThrow_whenProjectNotFound() {
            when(projectRepository.findProjectById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.findProjectById(999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));

            verify(projectMapper, never()).toProjectDto(any());
        }

        @Test
        @DisplayName("createProject should propagate validation errors from validator")
        void createProject_shouldThrow_whenProjectNumberExists() {
            ProjectCreationRequest request = new ProjectCreationRequest(
                    3116L, "Dup", "Customer", LocalDate.of(2026, 1, 1), null,
                    Collections.singletonList("QMV"), 10L
            );

            doThrow(new BusinessException(ErrorCode.PROJECT_NUMBER_EXISTS))
                    .when(projectValidator).validateCreateProject(request);

            assertThatThrownBy(() -> projectService.createProject(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NUMBER_EXISTS));

            verify(groupService, never()).findById(any());
            verify(projectRepository, never()).save(any());
        }

        @Test
        @DisplayName("createProject should skip employee lookup when visas list is empty")
        void createProject_shouldSkipEmployeeLookup_whenVisasEmpty() {
            ProjectCreationRequest request = new ProjectCreationRequest(
                    9002L, "No Members", "Customer", LocalDate.of(2026, 1, 1), null,
                    Collections.emptyList(), 10L
            );

            doNothing().when(projectValidator).validateCreateProject(request);
            when(groupService.findById(10L)).thenReturn(group);
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectMapper.toProjectSummary(any(Project.class))).thenReturn(projectSummaryDto);

            projectService.createProject(request);

            verify(employeeService, never()).findEmployeesByVisas(anyList());

            ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
            verify(projectRepository).save(projectCaptor.capture());
            assertThat(projectCaptor.getValue().getEmployees()).isEmpty();
        }

        @Test
        @DisplayName("updateProject should clear all members when visas list is empty")
        void updateProject_shouldClearEmployees_whenVisasEmpty() {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name", "Updated Customer", Status.NEW,
                    LocalDate.of(2026, 2, 1), null,
                    Collections.emptyList(), 10L
            );

            when(projectValidator.validateUpdateProject(100L, request)).thenReturn(project);
            when(groupService.findById(10L)).thenReturn(group);
            when(projectMapper.toProjectDto(project)).thenReturn(projectDto);

            projectService.updateProject(100L, request);

            assertThat(project.getEmployees()).isEmpty();
            verify(employeeService, never()).findEmployeesByVisas(anyList());
        }

        @Test
        @DisplayName("updateProject should not call employee service when visas are unchanged")
        void updateProject_shouldNotLookupEmployees_whenVisasUnchanged() {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name", "Updated Customer", Status.INP,
                    LocalDate.of(2026, 2, 1), null,
                    Collections.singletonList("QMV"), 10L
            );

            when(projectValidator.validateUpdateProject(100L, request)).thenReturn(project);
            when(groupService.findById(10L)).thenReturn(group);
            when(projectMapper.toProjectDto(project)).thenReturn(projectDto);

            projectService.updateProject(100L, request);

            verify(employeeService, never()).findEmployeesByVisas(anyList());
            assertThat(project.getEmployees()).containsExactly(employeeQmv);
        }

        @Test
        @DisplayName("updateProject should replace members when visas change completely")
        void updateProject_shouldReplaceEmployees_whenVisasChange() {
            project.getEmployees().clear();
            project.addEmployee(employeeQmv);

            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name", "Updated Customer", Status.INP,
                    LocalDate.of(2026, 2, 1), null,
                    Collections.singletonList("HNH"), 10L
            );

            when(projectValidator.validateUpdateProject(100L, request)).thenReturn(project);
            when(groupService.findById(10L)).thenReturn(group);
            when(employeeService.findEmployeesByVisas(Collections.singletonList("HNH")))
                    .thenReturn(Collections.singletonList(employeeHnh));
            when(projectMapper.toProjectDto(project)).thenReturn(projectDto);

            projectService.updateProject(100L, request);

            assertThat(project.getEmployees()).containsExactly(employeeHnh);
            verify(employeeService).findEmployeesByVisas(Collections.singletonList("HNH"));
        }

        @Test
        @DisplayName("updateProject should normalize mixed-case visas before syncing")
        void updateProject_shouldNormalizeVisas_whenMixedCaseProvided() {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name", "Updated Customer", Status.INP,
                    LocalDate.of(2026, 2, 1), null,
                    Arrays.asList("qmv", "hnh"), 10L
            );

            when(projectValidator.validateUpdateProject(100L, request)).thenReturn(project);
            when(groupService.findById(10L)).thenReturn(group);
            when(employeeService.findEmployeesByVisas(Collections.singletonList("HNH")))
                    .thenReturn(Collections.singletonList(employeeHnh));
            when(projectMapper.toProjectDto(project)).thenReturn(projectDto);

            projectService.updateProject(100L, request);

            verify(employeeService).findEmployeesByVisas(Collections.singletonList("HNH"));
            assertThat(project.getEmployees()).containsExactlyInAnyOrder(employeeQmv, employeeHnh);
        }

        @Test
        @DisplayName("deleteProject should throw when project does not exist")
        void deleteProject_shouldThrow_whenProjectNotFound() {
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.deleteProject(999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));

            verify(projectValidator, never()).validateDeleteProject(any());
            verify(projectRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteProject should throw when project status is not NEW")
        void deleteProject_shouldThrow_whenStatusNotNew() {
            project.setStatus(Status.INP);
            when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
            doThrow(new BusinessException(ErrorCode.PROJECT_DELETE_NOT_ALLOWED, "3116"))
                    .when(projectValidator).validateDeleteProject(project);

            assertThatThrownBy(() -> projectService.deleteProject(100L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_DELETE_NOT_ALLOWED));

            verify(projectRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteProjects should deduplicate ids before lookup and validation")
        void deleteProjects_shouldDeduplicateIds_whenDuplicatesProvided() {
            List<Long> distinctIds = Arrays.asList(100L, 101L);
            List<Project> projects = new ArrayList<>(distinctIds.size());

            when(projectRepository.findAllById(distinctIds)).thenReturn(projects);
            doNothing().when(projectValidator).validateDeleteProjects(projects, distinctIds);

            projectService.deleteProjects(Arrays.asList(100L, 101L, 100L));

            verify(projectRepository).findAllById(distinctIds);
            verify(projectValidator).validateDeleteProjects(projects, distinctIds);
            verify(projectRepository).deleteAll(projects);
        }

        @Test
        @DisplayName("deleteProjects should throw when any project id is missing")
        void deleteProjects_shouldThrow_whenProjectMissing() {
            List<Long> requestedIds = Arrays.asList(100L, 999L);
            when(projectRepository.findAllById(requestedIds)).thenReturn(Collections.singletonList(project));
            doThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND))
                    .when(projectValidator).validateDeleteProjects(Collections.singletonList(project), requestedIds);

            assertThatThrownBy(() -> projectService.deleteProjects(requestedIds))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));

            verify(projectRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("deleteProjects should throw when any selected project is not NEW")
        void deleteProjects_shouldThrow_whenAnyStatusNotNew() {
            Project nonDeletable = new Project();
            nonDeletable.setProjectNumber(7157L);
            nonDeletable.setStatus(Status.INP);
            ReflectionTestUtils.setField(nonDeletable, "id", 101L);

            List<Long> requestedIds = Arrays.asList(100L, 101L);
            List<Project> projects = Arrays.asList(project, nonDeletable);

            when(projectRepository.findAllById(requestedIds)).thenReturn(projects);
            doThrow(new BusinessException(ErrorCode.PROJECT_DELETE_NOT_ALLOWED, "7157"))
                    .when(projectValidator).validateDeleteProjects(projects, requestedIds);

            assertThatThrownBy(() -> projectService.deleteProjects(requestedIds))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_DELETE_NOT_ALLOWED));

            verify(projectRepository, never()).deleteAll(anyList());
        }
    }
}
