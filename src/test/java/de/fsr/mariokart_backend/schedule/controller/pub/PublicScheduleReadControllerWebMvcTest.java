package de.fsr.mariokart_backend.schedule.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.ScheduleOverviewDTO;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleOverviewService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicScheduleReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicScheduleReadControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicScheduleReadService publicScheduleReadService;

    @MockitoBean
    private PublicScheduleOverviewService publicScheduleOverviewService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getCurrentRoundsReturnsList() throws Exception {
        when(publicScheduleReadService.getCurrentRounds())
                .thenReturn(List.of(new RoundReturnDTO(1L, 1, null, null, false, false, Set.of(), null)));

        mockMvc.perform(get("/public/schedule/rounds/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getNumberOfRoundsUnplayedReturnsValue() throws Exception {
        when(publicScheduleReadService.getNumberOfRoundsUnplayed()).thenReturn(3);

        mockMvc.perform(get("/public/schedule/rounds/unplayed"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void isScheduleCreatedReturnsBoolean() throws Exception {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        mockMvc.perform(get("/public/schedule/create/schedule"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void getOverviewReturnsRoundsTeamsAndFlags() throws Exception {
        TeamReturnDTO pausing = new TeamReturnDTO(9L, "Team Yoshi", null, true, true, 0, 0, 2);
        when(publicScheduleOverviewService.getOverview()).thenReturn(new ScheduleOverviewDTO(
                List.of(new RoundReturnDTO(5L, 5, null, null, false, false, Set.of(), null)),
                Map.of(5L, List.of(pausing)), true, false, 4, true));

        mockMvc.perform(get("/public/schedule/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRounds[0].id").value(5))
                .andExpect(jsonPath("$.teamsNotInRound['5'][0].teamName").value("Team Yoshi"))
                .andExpect(jsonPath("$.scheduleCreated").value(true))
                .andExpect(jsonPath("$.finalScheduleCreated").value(false))
                .andExpect(jsonPath("$.numberOfRoundsUnplayed").value(4))
                .andExpect(jsonPath("$.tournamentOpen").value(true));
    }

    @Test
    void isFinalScheduleCreatedReturnsBoolean() throws Exception {
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);

        mockMvc.perform(get("/public/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
