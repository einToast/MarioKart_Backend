package de.fsr.mariokart_backend.registration.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminRegistrationDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminRegistrationDeleteControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminRegistrationDeleteService adminRegistrationDeleteService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void deleteTeamReturnsOk() throws Exception {
        mockMvc.perform(delete("/admin/teams/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTeamReturnsConflictWhenScheduleAlreadyExists() throws Exception {
        doThrow(new RoundsAlreadyExistsException("Schedule already exists"))
                .when(adminRegistrationDeleteService).deleteTeam(1L);

        mockMvc.perform(delete("/admin/teams/1"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Schedule already exists")));
    }

    @Test
    void deleteTeamReturnsNotFoundWhenTeamMissing() throws Exception {
        doThrow(new EntityNotFoundException("There is no team with this ID."))
                .when(adminRegistrationDeleteService).deleteTeam(1L);

        mockMvc.perform(delete("/admin/teams/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("team with this ID")));
    }

    @Test
    void deleteAllTeamsReturnsOk() throws Exception {
        mockMvc.perform(delete("/admin/teams"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAllTeamsReturnsConflictWhenScheduleAlreadyExists() throws Exception {
        doThrow(new RoundsAlreadyExistsException("Schedule already exists"))
                .when(adminRegistrationDeleteService).deleteAllTeams();

        mockMvc.perform(delete("/admin/teams"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Schedule already exists")));
    }
}
