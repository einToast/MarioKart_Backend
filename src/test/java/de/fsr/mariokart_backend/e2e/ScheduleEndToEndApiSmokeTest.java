package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class ScheduleEndToEndApiSmokeTest extends AbstractEndToEndApiSmokeTest {

    @Test
    void scheduleLifecycleSmoke() throws Exception {
        seedSixteenTeams();
        MockCookie adminCookie = loginAsAdmin();

        MvcResult createScheduleResult = mockMvc.perform(post("/admin/schedule/create/schedule").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(8)))
                .andReturn();

        long roundId = jsonElementFieldAsLong(createScheduleResult, 0, "id");

        mockMvc.perform(get("/public/schedule/create/schedule"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/public/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(get("/public/schedule/rounds/unplayed"))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));

        mockMvc.perform(get("/public/schedule/rounds/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)));

        mockMvc.perform(get("/admin/schedule/rounds").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(8)));

        mockMvc.perform(get("/admin/schedule/rounds/{roundId}", roundId).cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roundId));

        mockMvc.perform(get("/admin/schedule/break").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breakEnded").value(false));

        Round firstRound = roundRepository.findAll().stream()
                .sorted((left, right) -> Integer.compare(left.getRoundNumber(), right.getRoundNumber()))
                .findFirst()
                .orElseThrow();
        Game firstGame = gameRepository.findByRoundId(firstRound.getId()).getFirst();
        Points firstPoints = pointsRepository.findByGameId(firstGame.getId()).getFirst();

        mockMvc.perform(put("/admin/schedule/rounds/{roundId}/games/{gameId}/teams/{teamId}/points",
                        firstRound.getId(), firstGame.getId(), firstPoints.getTeam().getId())
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"points":12}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(12));

        mockMvc.perform(delete("/admin/schedule/create/schedule").cookie(adminCookie))
                .andExpect(status().isOk());

        assertThat(roundRepository.count()).isZero();
    }

    @Test
    void finalScheduleControllersSmoke() throws Exception {
        seedSixteenTeams();
        MockCookie adminCookie = loginAsAdmin();

        mockMvc.perform(post("/admin/schedule/create/schedule").cookie(adminCookie))
                .andExpect(status().isOk());

        List<Round> rounds = roundRepository.findAll();
        rounds.forEach(round -> round.setPlayed(true));
        roundRepository.saveAll(rounds);

        mockMvc.perform(post("/admin/schedule/create/final_schedule").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(11)));

        mockMvc.perform(get("/public/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(delete("/admin/schedule/create/final_schedule").cookie(adminCookie))
                .andExpect(status().isOk());

        assertThat(roundRepository.findByFinalGameTrue()).isEmpty();
        assertThat(roundRepository.count()).isEqualTo(8);
    }
}
