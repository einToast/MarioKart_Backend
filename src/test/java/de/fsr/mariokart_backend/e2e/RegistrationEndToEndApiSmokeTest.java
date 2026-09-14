package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class RegistrationEndToEndApiSmokeTest extends AbstractEndToEndApiSmokeTest {

    @Test
    void registrationLifecycleSmoke() throws Exception {
        mockMvc.perform(get("/public/teams/characters/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].characterName").exists());

        MvcResult createResult = mockMvc.perform(post("/public/teams")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"teamName":"Alpha","characterName":"Mario","finalReady":true,"active":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamName").value("Alpha"))
                .andReturn();

        long teamId = jsonFieldAsLong(createResult, "id");

        mockMvc.perform(get("/public/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha"));

        mockMvc.perform(get("/public/teams/sortedByTeamName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha"));

        MockCookie adminCookie = loginAsAdmin();

        mockMvc.perform(get("/admin/teams/finalTeams").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha"));

        mockMvc.perform(put("/admin/teams/{id}", teamId)
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"teamName":"Alpha Prime","characterName":"Mario","finalReady":false,"active":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Alpha Prime"))
                .andExpect(jsonPath("$.finalReady").value(false))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(put("/admin/teams/finalParticipation/reset").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].finalReady").value(true))
                .andExpect(jsonPath("$[0].active").value(true));

        mockMvc.perform(delete("/admin/teams/{id}", teamId).cookie(adminCookie))
                .andExpect(status().isOk());

        assertThat(teamRepository.count()).isZero();
    }

    @Test
    void adminRegistrationDeleteAllControllerSmoke() throws Exception {
        createTeamDirect("Alpha", "Mario");
        createTeamDirect("Beta", "Luigi");

        mockMvc.perform(delete("/admin/teams").cookie(loginAsAdmin()))
                .andExpect(status().isOk());

        assertThat(teamRepository.count()).isZero();
    }
}
