package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class FullTournamentApplicationEndToEndTest extends AbstractEndToEndApiSmokeTest {

    @Test
    void runsFullTournamentLifecycle() throws Exception {
        MockCookie adminCookie = loginAsAdmin();
        Map<String, Integer> teamStrength = new LinkedHashMap<>();
        List<String> availableCharacters = characterRepository.findAll().stream()
                .map(character -> character.getCharacterName())
                .filter(name -> !"Toad".equals(name))
                .limit(20)
                .toList();

        mockMvc.perform(get("/public/healthcheck"))
                .andExpect(status().isOk());

        updateTournamentSettings(adminCookie, true, true, 6);

        registerTeam("FSR", "Toad");
        teamStrength.put("FSR", 500);

        updateTournamentSettings(adminCookie, true, false, 6);
        updateTournamentSettings(adminCookie, true, true, 6);

        assertThat(availableCharacters).hasSize(20);

        long renamedTeamId = -1L;
        for (int index = 0; index < availableCharacters.size(); index++) {
            String teamName = index == 7 ? "RudeName" : "Team-%02d".formatted(index + 2);
            long teamId = registerTeam(teamName, availableCharacters.get(index));
            if ("RudeName".equals(teamName)) {
                renamedTeamId = teamId;
            }
            teamStrength.put(teamName, 400 - index);
        }

        assertThat(teamRepository.count()).isEqualTo(21);
        assertThat(renamedTeamId).isPositive();

        renameTeam(adminCookie, renamedTeamId, "FairPlayCrew", "Waluigi");
        teamStrength.put("FairPlayCrew", teamStrength.remove("RudeName"));

        MvcResult scheduleCreateResult = mockMvc.perform(post("/admin/schedule/create/schedule")
                .cookie(adminCookie)
                .contentType(APPLICATION_JSON)
                .content("""
                        {"version":1,"numFields":4,"numRounds":8,"teamsPerGame":4}
                        """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createdRounds = JSON.readTree(scheduleCreateResult.getResponse().getContentAsString());
        assertThat(createdRounds.isArray()).isTrue();
        assertThat(createdRounds.size()).isGreaterThan(0);

        JsonNode initialStandings = getPublicGroupStandings();
        assertThat(initialStandings).hasSize(21);

        boolean breakMoved = false;
        boolean breakCompleted = false;
        int playedGroupRounds = 0;

        while (getUnplayedRoundCount() > 0) {
            JsonNode currentRounds = getCurrentRounds();
            assertThat(currentRounds.isArray()).isTrue();
            assertThat(currentRounds.size()).isBetween(1, 2);

            JsonNode standings = getPublicGroupStandings();
            assertThat(standings).hasSize(21);

            long currentRoundId = currentRounds.get(0).path("id").asLong();

            if (playedGroupRounds == 5 && !breakMoved) {
                JsonNode currentBreak = getBreak(adminCookie);
                int currentBreakRoundNumber = currentBreak.path("round").path("roundNumber").asInt();
                JsonNode allRounds = getAdminRounds(adminCookie);
                JsonNode movedBreakRound = allRounds.findValues("roundNumber").stream()
                        .filter(node -> node.asInt() == currentBreakRoundNumber + 1)
                        .findFirst()
                        .flatMap(numberNode -> findRoundByRoundNumber(allRounds, numberNode.asInt()))
                        .orElseThrow();

                JsonNode movedBreak = updateBreak(adminCookie,
                        movedBreakRound.path("id").asLong(),
                        30,
                        false);
                assertThat(movedBreak.path("round").path("roundNumber").asInt()).isEqualTo(currentBreakRoundNumber + 1);
                breakMoved = true;
            }

            JsonNode currentBreak = getBreak(adminCookie);
            if (currentBreak.path("round").path("id").asLong() == currentRoundId
                    && !currentBreak.path("breakEnded").asBoolean()) {
                JsonNode completedBreak = updateBreak(adminCookie,
                        currentBreak.path("round").path("id").asLong(),
                        30,
                        true);
                assertThat(completedBreak.path("breakEnded").asBoolean()).isTrue();
                breakCompleted = true;
            }

            JsonNode roundDetails = getAdminRound(adminCookie, currentRoundId);
            applyScoresToRound(adminCookie, roundDetails, teamStrength);

            markRoundPlayed(adminCookie, currentRoundId);
            playedGroupRounds++;

            JsonNode updatedStandings = getPublicGroupStandings();
            assertThat(updatedStandings).hasSize(21);
            assertThat(teamNames(updatedStandings)).contains("FSR");

            if (getUnplayedRoundCount() > 0) {
                JsonNode nextCurrentRounds = getCurrentRounds();
                assertThat(nextCurrentRounds.get(0).path("id").asLong()).isNotEqualTo(currentRoundId);
            }
        }

        assertThat(breakMoved).isTrue();
        assertThat(breakCompleted).isTrue();

        JsonNode finalists = getFinalTeams(adminCookie);
        assertThat(finalists).hasSize(4);
        assertThat(teamNames(finalists)).contains("FSR");

        mockMvc.perform(post("/admin/schedule/create/final_schedule").cookie(adminCookie))
                .andExpect(status().isOk());

        while (getUnplayedRoundCount() > 0) {
            JsonNode currentRounds = getCurrentRounds();
            long currentRoundId = currentRounds.get(0).path("id").asLong();

            JsonNode roundDetails = getAdminRound(adminCookie, currentRoundId);
            applyScoresToRound(adminCookie, roundDetails, teamStrength);
            markRoundPlayed(adminCookie, currentRoundId);

            JsonNode finalStandingsSnapshot = getFinalStandings(adminCookie);
            assertThat(finalStandingsSnapshot).hasSize(21);
        }

        JsonNode finalStandings = getFinalStandings(adminCookie);
        assertThat(finalStandings).hasSize(21);
        assertThat(finalStandings.get(0).path("teamName").asText()).isEqualTo("FSR");
        assertThat(finalStandings.get(0).path("finalPoints").asInt()).isPositive();
    }

    private void updateTournamentSettings(MockCookie adminCookie, boolean tournamentOpen, boolean registrationOpen,
            int maxGamesCount)
            throws Exception {
        mockMvc.perform(put("/admin/settings")
                .cookie(adminCookie)
                .contentType(APPLICATION_JSON)
                .content("""
                        {"tournamentOpen":%s,"registrationOpen":%s,"maxGamesCount":%d}
                        """.formatted(tournamentOpen, registrationOpen, maxGamesCount)))
                .andExpect(status().isOk());
    }

    private long registerTeam(String teamName, String characterName) throws Exception {
        MvcResult result = mockMvc.perform(post("/public/teams")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"teamName":"%s","characterName":"%s","finalReady":true,"active":true}
                        """.formatted(teamName, characterName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamName").value(teamName))
                .andReturn();

        return jsonFieldAsLong(result, "id");
    }

    private void renameTeam(MockCookie adminCookie, long teamId, String newName, String characterName)
            throws Exception {
        mockMvc.perform(put("/admin/teams/{id}", teamId)
                .cookie(adminCookie)
                .contentType(APPLICATION_JSON)
                .content("""
                        {"teamName":"%s","characterName":"%s","finalReady":true,"active":true}
                        """.formatted(newName, characterName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value(newName));
    }

    private JsonNode getCurrentRounds() throws Exception {
        MvcResult result = mockMvc.perform(get("/public/schedule/rounds/current"))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private int getUnplayedRoundCount() throws Exception {
        MvcResult result = mockMvc.perform(get("/public/schedule/rounds/unplayed"))
                .andExpect(status().isOk())
                .andReturn();
        return Integer.parseInt(result.getResponse().getContentAsString());
    }

    private JsonNode getPublicGroupStandings() throws Exception {
        MvcResult result = mockMvc.perform(get("/public/teams/sortedByGroupPoints"))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getAdminRounds(MockCookie adminCookie) throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/schedule/rounds").cookie(adminCookie))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getAdminRound(MockCookie adminCookie, long roundId) throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/schedule/rounds/{roundId}", roundId).cookie(adminCookie))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getBreak(MockCookie adminCookie) throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/schedule/break").cookie(adminCookie))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode updateBreak(MockCookie adminCookie, long roundId, int duration, boolean breakEnded)
            throws Exception {
        MvcResult result = mockMvc.perform(put("/admin/schedule/break")
                .cookie(adminCookie)
                .contentType(APPLICATION_JSON)
                .content("""
                        {"roundId":%d,"breakDuration":%d,"breakEnded":%s}
                        """.formatted(roundId, duration, breakEnded)))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private void applyScoresToRound(MockCookie adminCookie, JsonNode roundDetails, Map<String, Integer> teamStrength)
            throws Exception {
        for (JsonNode gameNode : roundDetails.path("games")) {
            long gameId = gameNode.path("id").asLong();
            for (JsonNode pointsNode : gameNode.path("points")) {
                long teamId = pointsNode.path("team").path("id").asLong();
                String teamName = pointsNode.path("team").path("teamName").asText();
                int score = teamStrength.getOrDefault(teamName, 1);

                mockMvc.perform(put("/admin/schedule/rounds/{roundId}/games/{gameId}/teams/{teamId}/points",
                        roundDetails.path("id").asLong(), gameId, teamId)
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"points":%d}
                                """.formatted(score)))
                        .andExpect(status().isOk());
            }
        }
    }

    private void markRoundPlayed(MockCookie adminCookie, long roundId) throws Exception {
        mockMvc.perform(put("/admin/schedule/rounds/{roundId}", roundId)
                .cookie(adminCookie)
                .contentType(APPLICATION_JSON)
                .content("""
                        {"played":true}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.played").value(true));
    }

    private JsonNode getFinalTeams(MockCookie adminCookie) throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/teams/finalTeams").cookie(adminCookie))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getFinalStandings(MockCookie adminCookie) throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/teams/sortedByFinalPoints").cookie(adminCookie))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private List<String> teamNames(JsonNode teams) {
        return IntStream.range(0, teams.size())
                .mapToObj(index -> teams.get(index).path("teamName").asText())
                .toList();
    }

    private java.util.Optional<JsonNode> findRoundByRoundNumber(JsonNode rounds, int roundNumber) {
        return IntStream.range(0, rounds.size())
                .mapToObj(rounds::get)
                .filter(round -> round.path("roundNumber").asInt() == roundNumber)
                .findFirst();
    }
}
