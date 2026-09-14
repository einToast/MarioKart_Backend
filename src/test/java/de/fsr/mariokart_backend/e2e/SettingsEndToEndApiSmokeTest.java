package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class SettingsEndToEndApiSmokeTest extends AbstractEndToEndApiSmokeTest {

    @Test
    void settingsControllersSmoke() throws Exception {
        mockMvc.perform(get("/public/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentOpen").value(true))
                .andExpect(jsonPath("$.registrationOpen").value(true))
                .andExpect(jsonPath("$.maxGamesCount").value(6));

        MockCookie adminCookie = loginAsAdmin();

        mockMvc.perform(put("/admin/settings")
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"tournamentOpen":true,"registrationOpen":true,"maxGamesCount":8}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxGamesCount").value(8));

        createTeamDirect("Reset Team", "Mario");
        createRoundDirect(1, false, false);
        MultipleChoiceQuestion question = new MultipleChoiceQuestion();
        question.setOptions(List.of("A", "B"));
        questionRepository.save(question);

        mockMvc.perform(delete("/admin/settings/reset").cookie(adminCookie))
                .andExpect(status().isOk());

        assertThat(teamRepository.count()).isZero();
        assertThat(roundRepository.count()).isZero();
        assertThat(questionRepository.count()).isZero();
        assertThat(tournamentRepository.count()).isEqualTo(1);
    }
}
