package de.fsr.mariokart_backend.schedule.controller.pub;

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

import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicScheduleReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicScheduleReadControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/schedule/public-schedule-read-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicScheduleReadService publicScheduleReadService;

    @Test
    void getCurrentRoundsMatchesContract() throws Exception {
        when(publicScheduleReadService.getCurrentRounds())
                .thenReturn(List.of(new RoundReturnDTO(1L, 1, null, null, false, false, Set.of(), null)));

        MvcResult result = mockMvc.perform(get("/public/schedule/rounds/current"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_rounds_current_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getNumberOfRoundsUnplayedMatchesContract() throws Exception {
        when(publicScheduleReadService.getNumberOfRoundsUnplayed()).thenReturn(3);

        MvcResult result = mockMvc.perform(get("/public/schedule/rounds/unplayed"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_rounds_unplayed_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void isScheduleCreatedMatchesContract() throws Exception {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        MvcResult result = mockMvc.perform(get("/public/schedule/create/schedule"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_create_schedule_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void isFinalScheduleCreatedMatchesContract() throws Exception {
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);

        MvcResult result = mockMvc.perform(get("/public/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_create_final_schedule_success",
                result.getResponse().getContentAsString());
    }
}
