package de.fsr.mariokart_backend.settings.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;

@WebMvcTest(PublicSettingsReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicSettingsReadControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicSettingsReadService publicSettingsReadService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getSettingsReturnsTournamentDto() throws Exception {
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, true, 6));

        mockMvc.perform(get("/public/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentOpen").value(true))
                .andExpect(jsonPath("$.registrationOpen").value(true))
                .andExpect(jsonPath("$.maxGamesCount").value(6));
    }

    @Test
    void getSettingsReturnsNotFoundWhenMissing() throws Exception {
        when(publicSettingsReadService.getSettings()).thenThrow(new IllegalStateException("Settings do not exist."));

        mockMvc.perform(get("/public/settings"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Settings do not exist")));
    }
}
