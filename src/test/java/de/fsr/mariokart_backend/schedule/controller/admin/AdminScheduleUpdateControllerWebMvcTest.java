package de.fsr.mariokart_backend.schedule.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminScheduleUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminScheduleUpdateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminScheduleUpdateService adminScheduleUpdateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void updateRoundPlayedReturnsRound() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, null, null, false, true, Set.of(), null));

        mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateRoundPlayedMapsNotFound() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenThrow(new EntityNotFoundException("There is no round with this ID."));

        mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("round with this ID")));
    }

    @Test
    void updateRoundPlayedMapsConflict() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenThrow(new RoundsAlreadyExistsException("Break not finished."));

        mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Break not finished")));
    }

    @Test
    void updateRoundPlayedMapsInternalServerError() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenThrow(new NotificationNotSentException("Notification failed"));

        mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Notification failed")));
    }

    @Test
    void updatePointsReturnsPoints() throws Exception {
        when(adminScheduleUpdateService.updatePoints(anyLong(), anyLong(), anyLong(), any(PointsInputDTO.class)))
                .thenReturn(new PointsReturnDTO(3L, 12, null));

        mockMvc.perform(put("/admin/schedule/rounds/1/games/2/teams/3/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PointsInputDTO(12))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void updatePointsMapsNotFound() throws Exception {
        when(adminScheduleUpdateService.updatePoints(anyLong(), anyLong(), anyLong(), any(PointsInputDTO.class)))
                .thenThrow(new EntityNotFoundException("There are no points with this ID."));

        mockMvc.perform(put("/admin/schedule/rounds/1/games/2/teams/3/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PointsInputDTO(12))))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("points")));
    }

    @Test
    void updateBreakReturnsBreak() throws Exception {
        when(adminScheduleUpdateService.updateBreak(any(BreakInputDTO.class)))
                .thenReturn(new BreakReturnDTO(1L, null, null, false, null));

        mockMvc.perform(put("/admin/schedule/break")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BreakInputDTO(1L, 5, false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateBreakMapsNotFound() throws Exception {
        when(adminScheduleUpdateService.updateBreak(any(BreakInputDTO.class)))
                .thenThrow(new EntityNotFoundException("Schedule not created yet."));

        mockMvc.perform(put("/admin/schedule/break")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BreakInputDTO(1L, 5, false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBreakMapsInternalServerError() throws Exception {
        when(adminScheduleUpdateService.updateBreak(any(BreakInputDTO.class)))
                .thenThrow(new NotificationNotSentException("Notification failed"));

        mockMvc.perform(put("/admin/schedule/break")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BreakInputDTO(1L, 5, false))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Notification failed")));
    }

    @Test
    void updateRoundFullReturnsRound() throws Exception {
        when(adminScheduleUpdateService.updateRound(anyLong(), any(RoundInputFullDTO.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, null, null, false, true, Set.of(), null));

        mockMvc.perform(put("/admin/schedule/rounds/1/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputFullDTO(true, new GameInputFullDTO[0]))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateRoundFullMapsConflict() throws Exception {
        when(adminScheduleUpdateService.updateRound(anyLong(), any(RoundInputFullDTO.class)))
                .thenThrow(new RoundsAlreadyExistsException("Break not finished."));

        mockMvc.perform(put("/admin/schedule/rounds/1/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputFullDTO(true, new GameInputFullDTO[0]))))
                .andExpect(status().isConflict());
    }

    @Test
    void updateGameReturnsGame() throws Exception {
        when(adminScheduleUpdateService.updateGame(anyLong(), any(GameInputFullDTO.class)))
                .thenReturn(new GameReturnDTO(2L, "Blue", Set.of(), Set.of()));

        mockMvc.perform(put("/admin/schedule/games/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameInputFullDTO(2L, new de.fsr.mariokart_backend.schedule.model.dto.PointsInputFullDTO[0]))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void updateGameMapsNotFound() throws Exception {
        when(adminScheduleUpdateService.updateGame(anyLong(), any(GameInputFullDTO.class)))
                .thenThrow(new EntityNotFoundException("Es gibt kein Spiel mit dieser ID."));

        mockMvc.perform(put("/admin/schedule/games/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameInputFullDTO(2L, new de.fsr.mariokart_backend.schedule.model.dto.PointsInputFullDTO[0]))))
                .andExpect(status().isNotFound());
    }
}
