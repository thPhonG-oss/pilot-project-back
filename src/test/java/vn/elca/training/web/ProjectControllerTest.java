package vn.elca.training.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.elca.training.configuration.I18nConfiguration;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectDeleteRequest;
import vn.elca.training.model.dto.request.ProjectSearchCondition;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Status;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.model.exception.GlobalExceptionHandler;
import vn.elca.training.service.MessageService;
import vn.elca.training.service.ProjectService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({I18nConfiguration.class, GlobalExceptionHandler.class})
@DisplayName("ProjectController Unit Tests")
class ProjectControllerTest {

  private static final String PROJECTS_API = "/api/v1/projects";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProjectService projectService;

  @MockBean
  private MessageService messageService;

  private ProjectDto projectDto;

  @BeforeEach
  void setUp() {
    reset(projectService);

    projectDto = new ProjectDto(
        100L,
        3116L,
        "Facturation",
        "Les Rataites Populaires",
        Status.NEW,
        LocalDate.of(2004, 2, 25),
        null,
        null,
        null,
        2L
    );

    when(messageService.getMessage(any(String.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(messageService.getMessage(any(String.class), any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private ProjectCreationRequest validCreationRequest() {
    return new ProjectCreationRequest(
        9001L,
        "New Project",
        "New Customer",
        LocalDate.of(2026, 1, 1),
        null,
        Collections.singletonList("QMV"),
        10L
    );
  }

  private ProjectUpdateRequest validUpdateRequest() {
    return new ProjectUpdateRequest(
        "Updated Name",
        "Updated Customer",
        Status.INP,
        LocalDate.of(2026, 2, 1),
        null,
        Collections.singletonList("QMV"),
        10L,
        2L
    );
  }

  @Nested
  @DisplayName("POST /api/v1/projects")
  class CreateProject {

    @Test
    @DisplayName("Should return 201 with created project when request is valid")
    void createProject_shouldReturn201_whenRequestIsValid() throws Exception {
      when(projectService.createProject(any(ProjectCreationRequest.class))).thenReturn(projectDto);

      mockMvc.perform(post(PROJECTS_API)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validCreationRequest())))
          .andExpect(status().isCreated())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(100))
          .andExpect(jsonPath("$.projectNumber").value(3116))
          .andExpect(jsonPath("$.name").value("Facturation"))
          .andExpect(jsonPath("$.customer").value("Les Rataites Populaires"))
          .andExpect(jsonPath("$.status").value("NEW"));

      ArgumentCaptor<ProjectCreationRequest> requestCaptor = ArgumentCaptor.forClass(ProjectCreationRequest.class);
      verify(projectService).createProject(requestCaptor.capture());
      assertThat(requestCaptor.getValue().getProjectNumber()).isEqualTo(9001L);
      assertThat(requestCaptor.getValue().getName()).isEqualTo("New Project");
    }

    @Test
    @DisplayName("Should return 400 when mandatory fields are missing")
    void createProject_shouldReturn400_whenMandatoryFieldsMissing() throws Exception {
      ProjectCreationRequest invalidRequest = new ProjectCreationRequest(
          null, "", null, null, null, null, null
      );

      mockMvc.perform(post(PROJECTS_API)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details").isArray())
          .andExpect(jsonPath("$.details[0].field").exists());

      verify(projectService, never()).createProject(any());
    }

    @Test
    @DisplayName("Should return 400 when project number exceeds 4 digits")
    void createProject_shouldReturn400_whenProjectNumberTooLarge() throws Exception {
      ProjectCreationRequest invalidRequest = validCreationRequest();
      invalidRequest.setProjectNumber(10000L);

      mockMvc.perform(post(PROJECTS_API)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'projectNumber')]").exists());

      verify(projectService, never()).createProject(any());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/projects/search")
  class SearchProjects {

    @Test
    @DisplayName("Should return 200 with search results and default pagination")
    void search_shouldReturn200_whenCriteriaProvided() throws Exception {
      Page<ProjectDto> projectPage = new PageImpl<>(
          Collections.singletonList(projectDto),
          PageRequest.of(0, 10),
          1
      );
      when(projectService.findProjectsByCriteria(any(ProjectSearchCondition.class), any(Pageable.class)))
          .thenReturn(projectPage);

      mockMvc.perform(get(PROJECTS_API + "/search")
              .param("keyword", "fact")
              .param("status", "NEW")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(1))
          .andExpect(jsonPath("$.data[0].name").value("Facturation"));

      ArgumentCaptor<ProjectSearchCondition> conditionCaptor = ArgumentCaptor.forClass(ProjectSearchCondition.class);
      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(projectService).findProjectsByCriteria(conditionCaptor.capture(), pageableCaptor.capture());
      assertThat(conditionCaptor.getValue().getKeyword()).isEqualTo("fact");
      assertThat(conditionCaptor.getValue().getStatus()).isEqualTo(Status.NEW);
      assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
      assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should pass custom page and size to service")
    void search_shouldUseCustomPageAndSize_whenProvided() throws Exception {
      when(projectService.findProjectsByCriteria(any(ProjectSearchCondition.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      mockMvc.perform(get(PROJECTS_API + "/search")
              .param("page", "2")
              .param("size", "20")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());

      ArgumentCaptor<ProjectSearchCondition> conditionCaptor = ArgumentCaptor.forClass(ProjectSearchCondition.class);
      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(projectService).findProjectsByCriteria(conditionCaptor.capture(), pageableCaptor.capture());
      assertThat(conditionCaptor.getValue().getKeyword()).isNull();
      assertThat(conditionCaptor.getValue().getStatus()).isNull();
      assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
      assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
      assertThat(pageableCaptor.getValue().getSort().getOrderFor("projectNumber").getDirection())
          .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("Should return 400 when keyword exceeds max length")
    void search_shouldReturn400_whenKeywordTooLong() throws Exception {
      String longKeyword = String.join("", Collections.nCopies(51, "a"));

      mockMvc.perform(get(PROJECTS_API + "/search")
              .param("keyword", longKeyword)
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details").isArray());

      verify(projectService, never()).findProjectsByCriteria(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should bind advanced filters: leader visa, member visa and date ranges")
    void search_shouldBindAdvancedFilters_whenProvided() throws Exception {
      when(projectService.findProjectsByCriteria(any(ProjectSearchCondition.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      mockMvc.perform(get(PROJECTS_API + "/search")
              .param("leaderVisa", "QMV")
              .param("memberVisa", "HNH")
              .param("startDateFrom", "2026-01-01")
              .param("startDateTo", "2026-12-31")
              .param("endDateFrom", "2026-02-01")
              .param("endDateTo", "2026-11-30")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());

      ArgumentCaptor<ProjectSearchCondition> conditionCaptor = ArgumentCaptor.forClass(ProjectSearchCondition.class);
      verify(projectService).findProjectsByCriteria(conditionCaptor.capture(), any(Pageable.class));

      ProjectSearchCondition captured = conditionCaptor.getValue();
      assertThat(captured.getLeaderVisa()).isEqualTo("QMV");
      assertThat(captured.getMemberVisa()).isEqualTo("HNH");
      assertThat(captured.getStartDateFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
      assertThat(captured.getStartDateTo()).isEqualTo(LocalDate.of(2026, 12, 31));
      assertThat(captured.getEndDateFrom()).isEqualTo(LocalDate.of(2026, 2, 1));
      assertThat(captured.getEndDateTo()).isEqualTo(LocalDate.of(2026, 11, 30));
    }

    @Test
    @DisplayName("Should return 400 when leader visa exceeds max length")
    void search_shouldReturn400_whenLeaderVisaTooLong() throws Exception {
      mockMvc.perform(get(PROJECTS_API + "/search")
              .param("leaderVisa", "TOOLONG")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'leaderVisa')]").exists());

      verify(projectService, never()).findProjectsByCriteria(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return 400 when search start date range is invalid")
    void search_shouldReturn400_whenStartDateRangeInvalid() throws Exception {
      when(projectService.findProjectsByCriteria(any(ProjectSearchCondition.class), any(Pageable.class)))
          .thenThrow(new BusinessException(ErrorCode.INVALID_SEARCH_START_DATE_RANGE));

      mockMvc.perform(get(PROJECTS_API + "/search")
              .param("startDateFrom", "2026-12-31")
              .param("startDateTo", "2026-01-01")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("1013"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/projects/{id}")
  class FindProjectById {

    @Test
    @DisplayName("Should return 200 with project when id is valid")
    void findProjectById_shouldReturn200_whenProjectExists() throws Exception {
      when(projectService.findProjectById(100L)).thenReturn(projectDto);

      mockMvc.perform(get(PROJECTS_API + "/100").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(100))
          .andExpect(jsonPath("$.projectNumber").value(3116));

      verify(projectService).findProjectById(100L);
    }

    @Test
    @DisplayName("Should return 400 when id is not positive")
    void findProjectById_shouldReturn400_whenIdNotPositive() throws Exception {
      mockMvc.perform(get(PROJECTS_API + "/0").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details").isArray());

      verify(projectService, never()).findProjectById(any());
    }

    @Test
    @DisplayName("Should return 404 when project does not exist")
    void findProjectById_shouldReturn404_whenProjectNotFound() throws Exception {
      when(projectService.findProjectById(999L))
          .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

      mockMvc.perform(get(PROJECTS_API + "/999").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("1001"));
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/projects/{id}")
  class UpdateProject {

    @Test
    @DisplayName("Should return 200 when request is valid")
    void updateProject_shouldReturn200_whenRequestIsValid() throws Exception {
      doNothing().when(projectService).updateProject(eq(100L), any(ProjectUpdateRequest.class));

      mockMvc.perform(put(PROJECTS_API + "/100")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validUpdateRequest())))
          .andExpect(status().isOk());

      ArgumentCaptor<ProjectUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ProjectUpdateRequest.class);
      verify(projectService).updateProject(eq(100L), requestCaptor.capture());
      assertThat(requestCaptor.getValue().getName()).isEqualTo("Updated Name");
      assertThat(requestCaptor.getValue().getStatus()).isEqualTo(Status.INP);
      assertThat(requestCaptor.getValue().getVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return 409 when optimistic lock conflict occurs")
    void updateProject_shouldReturn409_whenVersionConflict() throws Exception {
      doThrow(new OptimisticLockingFailureException("Version changes are not equal"))
          .when(projectService).updateProject(eq(100L), any(ProjectUpdateRequest.class));

      mockMvc.perform(put(PROJECTS_API + "/100")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validUpdateRequest())))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("1010"));
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid")
    void updateProject_shouldReturn400_whenRequestBodyInvalid() throws Exception {
      ProjectUpdateRequest invalidRequest = new ProjectUpdateRequest(
          "", "", null, null, null, null, null, null
      );

      mockMvc.perform(put(PROJECTS_API + "/100")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details").isArray());

      verify(projectService, never()).updateProject(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when path id is not positive")
    void updateProject_shouldReturn400_whenPathIdNotPositive() throws Exception {
      mockMvc.perform(put(PROJECTS_API + "/0")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validUpdateRequest())))
          .andExpect(status().isBadRequest());

      verify(projectService, never()).updateProject(any(), any());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/projects/{id}")
  class DeleteProject {

    @Test
    @DisplayName("Should return 204 when deletion succeeds")
    void deleteProject_shouldReturn204_whenDeletionSucceeds() throws Exception {
      doNothing().when(projectService).deleteProject(100L);

      mockMvc.perform(delete(PROJECTS_API + "/100"))
          .andExpect(status().isNoContent());

      verify(projectService).deleteProject(100L);
    }

    @Test
    @DisplayName("Should return 400 when id is not positive")
    void deleteProject_shouldReturn400_whenIdNotPositive() throws Exception {
      mockMvc.perform(delete(PROJECTS_API + "/0"))
          .andExpect(status().isBadRequest());

      verify(projectService, never()).deleteProject(any());
    }

    @Test
    @DisplayName("Should return 400 when project status is not NEW")
    void deleteProject_shouldReturn400_whenStatusNotNew() throws Exception {
      doThrow(new BusinessException(ErrorCode.PROJECT_DELETE_NOT_ALLOWED, "3116"))
          .when(projectService).deleteProject(100L);

      mockMvc.perform(delete(PROJECTS_API + "/100"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("1007"));
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/projects")
  class DeleteProjects {

    @Test
    @DisplayName("Should return 204 when batch deletion succeeds")
    void deleteProjects_shouldReturn204_whenDeletionSucceeds() throws Exception {
      List<Long> ids = Arrays.asList(100L, 101L);
      doNothing().when(projectService).deleteProjects(ids);

      ProjectDeleteRequest request = new ProjectDeleteRequest(ids);

      mockMvc.perform(delete(PROJECTS_API)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(projectService).deleteProjects(ids);
    }

    @Test
    @DisplayName("Should return 400 when ids list is empty")
    void deleteProjects_shouldReturn400_whenIdsEmpty() throws Exception {
      ProjectDeleteRequest request = new ProjectDeleteRequest(Collections.emptyList());

      mockMvc.perform(delete(PROJECTS_API)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details").isArray());

      verify(projectService, never()).deleteProjects(any());
    }

    @Test
    @DisplayName("Should return 400 when any id is not positive")
    void deleteProjects_shouldReturn400_whenIdNotPositive() throws Exception {
      ProjectDeleteRequest request = new ProjectDeleteRequest(Arrays.asList(100L, 0L));

      mockMvc.perform(delete(PROJECTS_API)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(projectService, never()).deleteProjects(any());
    }
  }
}
