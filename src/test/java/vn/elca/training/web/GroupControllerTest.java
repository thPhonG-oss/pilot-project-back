package vn.elca.training.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.service.GroupService;
import vn.elca.training.service.MessageService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@DisplayName("GroupController Unit Tests")
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private MessageService messageService;

    @BeforeEach
    void resetMocks() {
        reset(groupService);
    }

    @Nested
    @DisplayName("GET /api/v1/groups")
    class GetGroups {

        @Test
        @DisplayName("Should return 200 with group list when groups exist")
        void getGroups_shouldReturn200_whenGroupsExist() throws Exception {
            List<GroupDto> groups = Arrays.asList(
                    new GroupDto(10L, 1L, "QMV", "Quy Van"),
                    new GroupDto(20L, 1L, "HNH", "Hanh Ho")
            );
            when(groupService.findAll()).thenReturn(groups);

            mockMvc.perform(get("/api/v1/groups").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(10))
                    .andExpect(jsonPath("$[0].leaderVisa").value("QMV"))
                    .andExpect(jsonPath("$[0].leaderName").value("Quy Van"))
                    .andExpect(jsonPath("$[1].id").value(20))
                    .andExpect(jsonPath("$[1].leaderVisa").value("HNH"))
                    .andExpect(jsonPath("$[1].leaderName").value("Hanh Ho"));

            verify(groupService).findAll();
        }

        @Test
        @DisplayName("Should return 200 with empty array when no groups exist")
        void getGroups_shouldReturnEmptyList_whenNoGroups() throws Exception {
            when(groupService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/groups").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));

            verify(groupService).findAll();
        }
    }
}
