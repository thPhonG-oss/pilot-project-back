package vn.elca.training.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.elca.training.configuration.I18nConfiguration;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.service.EmployeeService;
import vn.elca.training.service.MessageService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(I18nConfiguration.class)
@DisplayName("EmployeeController Unit Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private MessageService messageService;

    @BeforeEach
    void resetMocks() {
        reset(employeeService);
    }

    @Nested
    @DisplayName("GET /api/v1/employees/suggestions")
    class SuggestEmployees {

        @Test
        @DisplayName("Should return 200 with matching employees when keyword is provided")
        void suggestEmployees_shouldReturn200_whenKeywordProvided() throws Exception {
            List<EmployeeDto> suggestions = Collections.singletonList(
                    new EmployeeDto(1L, "QMV", "Quy", "Van")
            );
            when(employeeService.suggestEmployees("qm")).thenReturn(suggestions);

            mockMvc.perform(get("/api/v1/employees/suggestions")
                            .param("keyword", "qm")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].visa").value("QMV"))
                    .andExpect(jsonPath("$[0].firstName").value("Quy"))
                    .andExpect(jsonPath("$[0].lastName").value("Van"));

            verify(employeeService).suggestEmployees("qm");
        }

        @Test
        @DisplayName("Should return 200 with multiple suggestions when several employees match")
        void suggestEmployees_shouldReturn200_whenMultipleMatches() throws Exception {
            List<EmployeeDto> suggestions = Arrays.asList(
                    new EmployeeDto(1L, "QMV", "Quy", "Van"),
                    new EmployeeDto(2L, "HNH", "Hanh", "Ho")
            );
            when(employeeService.suggestEmployees("h")).thenReturn(suggestions);

            mockMvc.perform(get("/api/v1/employees/suggestions")
                            .param("keyword", "h")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].visa").value("QMV"))
                    .andExpect(jsonPath("$[1].visa").value("HNH"));

            verify(employeeService).suggestEmployees("h");
        }

        @Test
        @DisplayName("Should return 200 with empty array when keyword is omitted")
        void suggestEmployees_shouldReturnEmptyList_whenKeywordOmitted() throws Exception {
            when(employeeService.suggestEmployees(isNull())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/employees/suggestions")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(employeeService).suggestEmployees(isNull());
        }

        @Test
        @DisplayName("Should return 200 with empty array when no employees match")
        void suggestEmployees_shouldReturnEmptyList_whenNoMatches() throws Exception {
            when(employeeService.suggestEmployees("zzz")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/employees/suggestions")
                            .param("keyword", "zzz")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(employeeService).suggestEmployees("zzz");
        }

        @Test
        @DisplayName("Should pass blank keyword to service as-is")
        void suggestEmployees_shouldDelegateBlankKeyword_toService() throws Exception {
            when(employeeService.suggestEmployees(eq("   "))).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/employees/suggestions")
                            .param("keyword", "   ")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(employeeService).suggestEmployees("   ");
        }

        @Test
        @DisplayName("Should return 400 when keyword exceeds max length")
        void suggestEmployees_shouldReturn400_whenKeywordTooLong() throws Exception {
            String longKeyword = String.join("", Collections.nCopies(51, "a"));

            mockMvc.perform(get("/api/v1/employees/suggestions")
                            .param("keyword", longKeyword)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").isArray());

            verify(employeeService, never()).suggestEmployees(anyString());
        }
    }
}
