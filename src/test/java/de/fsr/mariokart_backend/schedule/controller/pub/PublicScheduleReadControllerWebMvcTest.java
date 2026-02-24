package de.fsr.mariokart_backend.schedule.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
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
    void isFinalScheduleCreatedReturnsBoolean() throws Exception {
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);

        mockMvc.perform(get("/public/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
