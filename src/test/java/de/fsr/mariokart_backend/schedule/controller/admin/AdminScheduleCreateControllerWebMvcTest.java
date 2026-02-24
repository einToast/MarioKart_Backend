package de.fsr.mariokart_backend.schedule.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminScheduleCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminScheduleCreateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminScheduleCreateService adminScheduleCreateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void createScheduleReturnsRounds() throws Exception {
        when(adminScheduleCreateService.createSchedule()).thenReturn(List.of(round(1L, 1)));

        mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createScheduleReturnsConflictWhenScheduleExists() throws Exception {
        when(adminScheduleCreateService.createSchedule())
                .thenThrow(new RoundsAlreadyExistsException("Schedule already created"));

        mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Schedule already created")));
    }

    @Test
    void createScheduleReturnsNotFoundWhenTeamsMissing() throws Exception {
        when(adminScheduleCreateService.createSchedule())
                .thenThrow(new NotEnoughTeamsException("Not enough teams"));

        mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Not enough teams")));
    }

    @Test
    void createScheduleReturnsNotFoundWhenEntityMissing() throws Exception {
        when(adminScheduleCreateService.createSchedule())
                .thenThrow(new EntityNotFoundException("Missing"));

        mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Missing")));
    }

    @Test
    void createScheduleReturnsInternalServerErrorWhenNotificationFails() throws Exception {
        when(adminScheduleCreateService.createSchedule())
                .thenThrow(new NotificationNotSentException("Failed"));

        mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed")));
    }

    @Test
    void createFinalScheduleReturnsRounds() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule()).thenReturn(List.of(round(2L, 10)));

        mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void createFinalScheduleReturnsConflictWhenAlreadyExists() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new RoundsAlreadyExistsException("Final schedule already created"));

        mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Final schedule already created")));
    }

    @Test
    void createFinalScheduleReturnsBadRequestForIllegalArgument() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new IllegalArgumentException("Not all rounds played"));

        mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Not all rounds played")));
    }

    @Test
    void createFinalScheduleReturnsNotFoundWhenNotEnoughTeams() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new NotEnoughTeamsException("Not enough teams ready for final"));

        mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Not enough teams ready for final")));
    }

    @Test
    void createFinalScheduleReturnsInternalServerErrorWhenNotificationFails() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new NotificationNotSentException("Failed"));

        mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed")));
    }

    private static RoundReturnDTO round(Long id, int roundNumber) {
        return new RoundReturnDTO(id, roundNumber, null, null, false, false, Set.of(), null);
    }
}
