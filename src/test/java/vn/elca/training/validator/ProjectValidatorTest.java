package vn.elca.training.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.ProjectRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectValidator Unit Tests")
class ProjectValidatorTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectValidator projectValidator;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setProjectNumber(3116L);
        project.setName("Facturation");
        project.setCustomer("Les Rataites Populaires");
        project.setStatus(Status.NEW);
        project.setStartDate(LocalDate.of(2004, 2, 25));
        ReflectionTestUtils.setField(project, "id", 100L);
    }

    private ProjectCreationRequest createRequest(Long projectNumber, LocalDate start, LocalDate end) {
        return new ProjectCreationRequest(
                projectNumber,
                "New Project",
                "New Customer",
                start,
                end,
                Collections.singletonList("QMV"),
                10L
        );
    }

    private ProjectUpdateRequest updateRequest(LocalDate start, LocalDate end) {
        return new ProjectUpdateRequest(
                "Updated Name",
                "Updated Customer",
                Status.INP,
                start,
                end,
                Collections.singletonList("QMV"),
                10L
        );
    }

    @Nested
    @DisplayName("validateCreateProject")
    class ValidateCreateProject {

        @Test
        @DisplayName("Should pass when project number is unique and end date is null")
        void shouldPass_whenProjectNumberUniqueAndEndDateNull() {
            ProjectCreationRequest request = createRequest(9001L, LocalDate.of(2026, 1, 1), null);
            when(projectRepository.existsByProjectNumber(9001L)).thenReturn(false);

            assertThatCode(() -> projectValidator.validateCreateProject(request))
                    .doesNotThrowAnyException();

            verify(projectRepository).existsByProjectNumber(9001L);
        }

        @Test
        @DisplayName("Should pass when end date is after start date")
        void shouldPass_whenEndDateAfterStartDate() {
            ProjectCreationRequest request = createRequest(
                    9002L,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );
            when(projectRepository.existsByProjectNumber(9002L)).thenReturn(false);

            assertThatCode(() -> projectValidator.validateCreateProject(request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass when end date equals start date")
        void shouldPass_whenEndDateEqualsStartDate() {
            LocalDate sameDay = LocalDate.of(2026, 6, 1);
            ProjectCreationRequest request = createRequest(9003L, sameDay, sameDay);
            when(projectRepository.existsByProjectNumber(9003L)).thenReturn(false);

            assertThatCode(() -> projectValidator.validateCreateProject(request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw PROJECT_NUMBER_EXISTS when project number already exists")
        void shouldThrow_whenProjectNumberExists() {
            ProjectCreationRequest request = createRequest(3116L, LocalDate.of(2026, 1, 1), null);
            when(projectRepository.existsByProjectNumber(3116L)).thenReturn(true);

            assertThatThrownBy(() -> projectValidator.validateCreateProject(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NUMBER_EXISTS));
        }

        @Test
        @DisplayName("Should throw INVALID_END_DATE when end date is before start date")
        void shouldThrow_whenEndDateBeforeStartDate() {
            ProjectCreationRequest request = createRequest(
                    9004L,
                    LocalDate.of(2026, 12, 31),
                    LocalDate.of(2026, 1, 1)
            );
            when(projectRepository.existsByProjectNumber(9004L)).thenReturn(false);

            assertThatThrownBy(() -> projectValidator.validateCreateProject(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_END_DATE));
        }
    }

    @Nested
    @DisplayName("validateUpdateProject")
    class ValidateUpdateProject {

        @Test
        @DisplayName("Should return project when it exists and date range is valid")
        void shouldReturnProject_whenProjectExistsAndDatesValid() {
            ProjectUpdateRequest request = updateRequest(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );
            when(projectRepository.findProjectById(100L)).thenReturn(Optional.of(project));

            Project result = projectValidator.validateUpdateProject(100L, request);

            assertThat(result).isSameAs(project);
            verify(projectRepository).findProjectById(100L);
        }

        @Test
        @DisplayName("Should return project when end date is null")
        void shouldReturnProject_whenEndDateIsNull() {
            ProjectUpdateRequest request = updateRequest(LocalDate.of(2026, 1, 1), null);
            when(projectRepository.findProjectById(100L)).thenReturn(Optional.of(project));

            Project result = projectValidator.validateUpdateProject(100L, request);

            assertThat(result).isSameAs(project);
        }

        @Test
        @DisplayName("Should throw PROJECT_NOT_FOUND when project does not exist")
        void shouldThrow_whenProjectNotFound() {
            ProjectUpdateRequest request = updateRequest(LocalDate.of(2026, 1, 1), null);
            when(projectRepository.findProjectById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectValidator.validateUpdateProject(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
        }

        @Test
        @DisplayName("Should throw INVALID_END_DATE when end date is before start date")
        void shouldThrow_whenEndDateBeforeStartDate() {
            ProjectUpdateRequest request = updateRequest(
                    LocalDate.of(2026, 6, 15),
                    LocalDate.of(2026, 6, 1)
            );
            when(projectRepository.findProjectById(100L)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> projectValidator.validateUpdateProject(100L, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_END_DATE));
        }
    }

    @Nested
    @DisplayName("validateDeleteProject")
    class ValidateDeleteProject {

        @Test
        @DisplayName("Should pass when project status is NEW")
        void shouldPass_whenStatusIsNew() {
            project.setStatus(Status.NEW);

            assertThatCode(() -> projectValidator.validateDeleteProject(project))
                    .doesNotThrowAnyException();

            verify(projectRepository, never()).findProjectById(100L);
        }

        @Test
        @DisplayName("Should throw PROJECT_DELETE_NOT_ALLOWED when status is INP")
        void shouldThrow_whenStatusIsInp() {
            project.setStatus(Status.INP);

            assertThatThrownBy(() -> projectValidator.validateDeleteProject(project))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException businessException = (BusinessException) ex;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ErrorCode.PROJECT_DELETE_NOT_ALLOWED);
                        assertThat(businessException.getArgs()).containsExactly("3116");
                    });
        }

        @Test
        @DisplayName("Should throw PROJECT_DELETE_NOT_ALLOWED when status is FIN")
        void shouldThrow_whenStatusIsFin() {
            project.setStatus(Status.FIN);

            assertThatThrownBy(() -> projectValidator.validateDeleteProject(project))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_DELETE_NOT_ALLOWED));
        }

        @Test
        @DisplayName("Should throw PROJECT_DELETE_NOT_ALLOWED when status is PLA")
        void shouldThrow_whenStatusIsPla() {
            project.setStatus(Status.PLA);

            assertThatThrownBy(() -> projectValidator.validateDeleteProject(project))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_DELETE_NOT_ALLOWED));
        }
    }

    @Nested
    @DisplayName("validateDeleteProjects")
    class ValidateDeleteProjects {

        @Test
        @DisplayName("Should pass when all projects exist and have NEW status")
        void shouldPass_whenAllProjectsExistAndNew() {
            Project secondProject = new Project();
            secondProject.setProjectNumber(3118L);
            secondProject.setStatus(Status.NEW);
            ReflectionTestUtils.setField(secondProject, "id", 101L);

            List<Long> requestedIds = Arrays.asList(100L, 101L);
            List<Project> foundProjects = Arrays.asList(project, secondProject);

            assertThatCode(() -> projectValidator.validateDeleteProjects(foundProjects, requestedIds))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw PROJECT_NOT_FOUND when any requested id is missing")
        void shouldThrow_whenAnyProjectIdMissing() {
            List<Long> requestedIds = Arrays.asList(100L, 999L);

            assertThatThrownBy(() -> projectValidator.validateDeleteProjects(
                    Collections.singletonList(project), requestedIds))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
        }

        @Test
        @DisplayName("Should throw PROJECT_NOT_FOUND when found list is empty")
        void shouldThrow_whenFoundListIsEmpty() {
            List<Long> requestedIds = Collections.singletonList(100L);

            assertThatThrownBy(() -> projectValidator.validateDeleteProjects(
                    Collections.emptyList(), requestedIds))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
        }

        @Test
        @DisplayName("Should throw PROJECT_DELETE_NOT_ALLOWED with project numbers of non-NEW projects")
        void shouldThrow_whenAnyProjectNotNew() {
            Project nonDeletable = new Project();
            nonDeletable.setProjectNumber(7157L);
            nonDeletable.setStatus(Status.INP);
            ReflectionTestUtils.setField(nonDeletable, "id", 101L);

            List<Long> requestedIds = Arrays.asList(100L, 101L);
            List<Project> foundProjects = Arrays.asList(project, nonDeletable);

            assertThatThrownBy(() -> projectValidator.validateDeleteProjects(foundProjects, requestedIds))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException businessException = (BusinessException) ex;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ErrorCode.PROJECT_DELETE_NOT_ALLOWED);
                        assertThat(businessException.getArgs()).containsExactly("7157");
                    });
        }

        @Test
        @DisplayName("Should list multiple non-NEW project numbers in error args")
        void shouldListMultipleProjectNumbers_whenSeveralNotNew() {
            project.setStatus(Status.INP);

            Project secondNonDeletable = new Project();
            secondNonDeletable.setProjectNumber(7174L);
            secondNonDeletable.setStatus(Status.FIN);
            ReflectionTestUtils.setField(secondNonDeletable, "id", 101L);

            List<Long> requestedIds = Arrays.asList(100L, 101L);
            List<Project> foundProjects = Arrays.asList(project, secondNonDeletable);

            assertThatThrownBy(() -> projectValidator.validateDeleteProjects(foundProjects, requestedIds))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException businessException = (BusinessException) ex;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ErrorCode.PROJECT_DELETE_NOT_ALLOWED);
                        assertThat(businessException.getArgs()[0].toString())
                                .contains("3116")
                                .contains("7174");
                    });
        }
    }
}
