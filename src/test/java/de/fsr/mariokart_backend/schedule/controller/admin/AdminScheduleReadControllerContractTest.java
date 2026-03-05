package de.fsr.mariokart_backend.schedule.controller.admin;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminScheduleReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminScheduleReadControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/schedule/admin-schedule-read-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminScheduleReadService adminScheduleReadService;

    @Test
    void getRoundsMatchesContract() throws Exception {
        when(adminScheduleReadService.getRounds())
                .thenReturn(List.of(new RoundReturnDTO(1L, 1, null, null, false, false, Set.of(), null)));

        MvcResult result = mockMvc.perform(get("/admin/schedule/rounds"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_rounds_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getRoundByIdMatchesContract() throws Exception {
        when(adminScheduleReadService.getRoundById(4L))
                .thenReturn(new RoundReturnDTO(4L, 4, null, null, false, false, Set.of(), null));

        MvcResult result = mockMvc.perform(get("/admin/schedule/rounds/4"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_rounds_roundId_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getRoundByIdNotFoundMatchesContract() throws Exception {
        when(adminScheduleReadService.getRoundById(99L)).thenThrow(new EntityNotFoundException("There is no round"));

        MvcResult result = mockMvc.perform(get("/admin/schedule/rounds/99"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_rounds_roundId_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void getBreakMatchesContract() throws Exception {
        when(adminScheduleReadService.getBreak()).thenReturn(new BreakReturnDTO(1L, null, null, false, null));

        MvcResult result = mockMvc.perform(get("/admin/schedule/break"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_break_success",
                result.getResponse().getContentAsString());
    }
}
