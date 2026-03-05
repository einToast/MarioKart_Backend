package de.fsr.mariokart_backend.schedule.controller.admin;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminScheduleCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminScheduleCreateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/schedule/admin-schedule-create-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminScheduleCreateService adminScheduleCreateService;

    @Test
    void createScheduleSuccessMatchesContract() throws Exception {
        when(adminScheduleCreateService.createSchedule()).thenReturn(List.of(round(1L, 1)));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "post_create_schedule_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void createScheduleConflictMatchesContract() throws Exception {
        when(adminScheduleCreateService.createSchedule())
                .thenThrow(new RoundsAlreadyExistsException("Schedule already created"));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_create_schedule_error_409",
                result.getResponse().getContentAsString());
    }

    @Test
    void createScheduleNotFoundMatchesContract() throws Exception {
        when(adminScheduleCreateService.createSchedule())
                .thenThrow(new NotEnoughTeamsException("Not enough teams"));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_create_schedule_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void createScheduleInternalServerErrorMatchesContract() throws Exception {
        when(adminScheduleCreateService.createSchedule())
                .thenThrow(new NotificationNotSentException("Failed"));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/schedule"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_create_schedule_error_500",
                result.getResponse().getContentAsString());
    }

    @Test
    void createFinalScheduleSuccessMatchesContract() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule()).thenReturn(List.of(round(2L, 10)));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "post_create_final_schedule_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void createFinalScheduleConflictMatchesContract() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new RoundsAlreadyExistsException("Final schedule already created"));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_create_final_schedule_error_409",
                result.getResponse().getContentAsString());
    }

    @Test
    void createFinalScheduleBadRequestMatchesContract() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new IllegalArgumentException("Not all rounds played"));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_create_final_schedule_error_400",
                result.getResponse().getContentAsString());
    }

    @Test
    void createFinalScheduleNotFoundMatchesContract() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new NotEnoughTeamsException("Not enough teams"));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_create_final_schedule_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void createFinalScheduleInternalServerErrorMatchesContract() throws Exception {
        when(adminScheduleCreateService.createFinalSchedule())
                .thenThrow(new NotificationNotSentException("Failed"));

        MvcResult result = mockMvc.perform(post("/admin/schedule/create/final_schedule"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_create_final_schedule_error_500",
                result.getResponse().getContentAsString());
    }

    private static RoundReturnDTO round(Long id, int roundNumber) {
        return new RoundReturnDTO(id, roundNumber, null, null, false, false, Set.of(), null);
    }
}
