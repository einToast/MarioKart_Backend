package de.fsr.mariokart_backend.schedule.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminScheduleUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminScheduleUpdateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/schedule/admin-schedule-update-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminScheduleUpdateService adminScheduleUpdateService;

    @Test
    void updateRoundPlayedSuccessMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, null, null, false, true, Set.of(), null));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "put_rounds_roundId_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateRoundPlayedNotFoundMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenThrow(new EntityNotFoundException("No round"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_rounds_roundId_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateRoundPlayedConflictMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenThrow(new RoundsAlreadyExistsException("Break not finished"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_rounds_roundId_error_409",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateRoundPlayedInternalServerErrorMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRoundPlayed(anyLong(), any(RoundInputDTO.class)))
                .thenThrow(new NotificationNotSentException("Notification failed"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputDTO(true))))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_rounds_roundId_error_500",
                result.getResponse().getContentAsString());
    }

    @Test
    void updatePointsSuccessMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updatePoints(anyLong(), anyLong(), anyLong(), any(PointsInputDTO.class)))
                .thenReturn(new PointsReturnDTO(3L, 12, null));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1/games/2/teams/3/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PointsInputDTO(12))))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(
                SCHEMA,
                "put_rounds_roundId_games_gameId_teams_teamId_points_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updatePointsNotFoundMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updatePoints(anyLong(), anyLong(), anyLong(), any(PointsInputDTO.class)))
                .thenThrow(new EntityNotFoundException("No points"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1/games/2/teams/3/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PointsInputDTO(12))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "put_rounds_roundId_games_gameId_teams_teamId_points_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateBreakSuccessMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateBreak(any(BreakInputDTO.class)))
                .thenReturn(new BreakReturnDTO(1L, null, null, false, null));

        MvcResult result = mockMvc.perform(put("/admin/schedule/break")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BreakInputDTO(1L, 5, false))))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "put_break_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateBreakNotFoundMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateBreak(any(BreakInputDTO.class)))
                .thenThrow(new EntityNotFoundException("No break"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/break")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BreakInputDTO(1L, 5, false))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_break_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateBreakInternalServerErrorMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateBreak(any(BreakInputDTO.class)))
                .thenThrow(new NotificationNotSentException("Notification failed"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/break")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BreakInputDTO(1L, 5, false))))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_break_error_500",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateRoundFullSuccessMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRound(anyLong(), any(RoundInputFullDTO.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, null, null, false, true, Set.of(), null));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputFullDTO(true, new GameInputFullDTO[0]))))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "put_rounds_roundId_full_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateRoundFullNotFoundMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRound(anyLong(), any(RoundInputFullDTO.class)))
                .thenThrow(new EntityNotFoundException("No round"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputFullDTO(true, new GameInputFullDTO[0]))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_rounds_roundId_full_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateRoundFullConflictMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRound(anyLong(), any(RoundInputFullDTO.class)))
                .thenThrow(new RoundsAlreadyExistsException("Break not finished"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputFullDTO(true, new GameInputFullDTO[0]))))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_rounds_roundId_full_error_409",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateRoundFullInternalServerErrorMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateRound(anyLong(), any(RoundInputFullDTO.class)))
                .thenThrow(new NotificationNotSentException("Notification failed"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/rounds/1/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoundInputFullDTO(true, new GameInputFullDTO[0]))))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_rounds_roundId_full_error_500",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateGameSuccessMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateGame(anyLong(), any(GameInputFullDTO.class)))
                .thenReturn(new GameReturnDTO(2L, "Blue", Set.of(), Set.of()));

        MvcResult result = mockMvc.perform(put("/admin/schedule/games/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameInputFullDTO(2L, new PointsInputFullDTO[0]))))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "put_games_gameId_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateGameNotFoundMatchesContract() throws Exception {
        when(adminScheduleUpdateService.updateGame(anyLong(), any(GameInputFullDTO.class)))
                .thenThrow(new EntityNotFoundException("No game"));

        MvcResult result = mockMvc.perform(put("/admin/schedule/games/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameInputFullDTO(2L, new PointsInputFullDTO[0]))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_games_gameId_error_404",
                result.getResponse().getContentAsString());
    }
}
