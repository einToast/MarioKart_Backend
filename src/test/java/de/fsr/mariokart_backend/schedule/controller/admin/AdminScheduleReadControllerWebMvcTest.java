package de.fsr.mariokart_backend.schedule.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminScheduleReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminScheduleReadControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminScheduleReadService adminScheduleReadService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getRoundsReturnsList() throws Exception {
        when(adminScheduleReadService.getRounds()).thenReturn(List.of(new RoundReturnDTO(1L, 1, null, null, false, false, Set.of(), null)));

        mockMvc.perform(get("/admin/schedule/rounds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getRoundByIdReturnsRound() throws Exception {
        when(adminScheduleReadService.getRoundById(4L)).thenReturn(new RoundReturnDTO(4L, 4, null, null, false, false, Set.of(), null));

        mockMvc.perform(get("/admin/schedule/rounds/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void getRoundByIdReturnsNotFoundWhenMissing() throws Exception {
        when(adminScheduleReadService.getRoundById(99L)).thenThrow(new EntityNotFoundException("There is no round with this ID."));

        mockMvc.perform(get("/admin/schedule/rounds/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("round with this ID")));
    }

    @Test
    void getBreakReturnsBreak() throws Exception {
        when(adminScheduleReadService.getBreak()).thenReturn(new BreakReturnDTO(1L, null, null, false, null));

        mockMvc.perform(get("/admin/schedule/break"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
