package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;

import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;
import de.fsr.mariokart_backend.notification.model.PushSubscription;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
                "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class FullTournamentApplicationEndToEndTest extends AbstractEndToEndApiSmokeTest {

        private static final int TEAM_COUNT = 21;
        private static final int SWITCH_COUNT = 4;
        private static final int ROUND_COUNT = 8;
        private static final int TEAMS_PER_GAME = 4;
        private static final int TEAMS_PER_ROUND = SWITCH_COUNT * TEAMS_PER_GAME;
        private static final String PROGRAM = """
                        [{"icon":"flag","time":"16:00 - 16:30","text":"Begrüßung"},{"icon":"pizza","time":"18:00 - 18:30","text":"Pizzapause"}]""";
        private static final String FLOOR_PLAN = """
                        {"elements":[{"type":"table","x":1,"y":1}]}""";

        @Test
        void runsFullTournamentLifecycle() throws Exception {
                MockCookie adminCookie = loginAsAdmin();

                mockMvc.perform(get("/public/healthcheck"))
                                .andExpect(status().isOk());

                configureVenue(adminCookie);
                Map<String, Integer> teamStrength = registerTeams(adminCookie);
                Map<String, Long> teamIds = teamIdsByName();
                subscribeAndNotify(adminCookie, teamIds);
                createGroupSchedule(adminCookie);
                answerGroupPhaseSurveys(adminCookie, teamIds);
                playGroupRounds(adminCookie, teamStrength);
                checkGroupStandings(adminCookie, teamStrength);
                playFinal(adminCookie, teamStrength, teamIds);
                resetApplication(adminCookie);
        }

        private void configureVenue(MockCookie adminCookie) throws Exception {
                putSettings(adminCookie, """
                                {"switches":[{"name":"Blau","color":"#9DAEDA"},{"name":"Rot","color":"#DA9DC9"},{"name":"Grün","color":"#9DDAB0"},{"name":"Gelb","color":"#DAD49D"}],"floorPlan":%s,"program":%s,"finalTeamsCount":4}
                                """.formatted(JSON.writeValueAsString(FLOOR_PLAN), JSON.writeValueAsString(PROGRAM)));

                JsonNode settings = getJson(get("/public/settings"));
                assertThat(settings.path("switches")).hasSize(SWITCH_COUNT);
                assertThat(settings.path("switches").get(2).path("name").asText()).isEqualTo("Grün");
                assertThat(settings.path("program").asText()).isEqualTo(PROGRAM);
                assertThat(settings.path("floorPlan").asText()).isEqualTo(FLOOR_PLAN);
                assertThat(settings.path("finalTeamsCount").asInt()).isEqualTo(4);
        }

        private Map<String, Integer> registerTeams(MockCookie adminCookie) throws Exception {
                Map<String, Integer> teamStrength = new LinkedHashMap<>();
                List<String> availableCharacters = characterRepository.findAll().stream()
                                .map(character -> character.getCharacterName())
                                .filter(name -> !"Toad".equals(name) && !"Waluigi".equals(name)
                                                && !"Toadette".equals(name))
                                .limit(20)
                                .toList();

                updateTournamentSettings(adminCookie, true, true, 6);

                registerTeam("FSR", "Toad");
                teamStrength.put("FSR", 500);

                updateTournamentSettings(adminCookie, true, false, 6);
                mockMvc.perform(post("/public/teams").with(csrf().asHeader())
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"teamName":"Too Late","characterName":"Mario","finalReady":true,"active":true}
                                                """))
                                .andExpect(status().isConflict());
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

                long noShowId = registerTeam("No-Show", "Toadette");
                assertThat(availableCharacterNames()).doesNotContain("Toad", "Toadette", availableCharacters.get(0));

                mockMvc.perform(post("/public/teams").with(csrf().asHeader())
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"teamName":"Copycat","characterName":"Toad","finalReady":true,"active":true}
                                                """))
                                .andExpect(status().isBadRequest());

                mockMvc.perform(delete("/admin/teams/{id}", noShowId).with(csrf().asHeader()).cookie(adminCookie))
                                .andExpect(status().isOk());
                assertThat(availableCharacterNames()).contains("Toadette");

                assertThat(teamRepository.count()).isEqualTo(TEAM_COUNT);
                assertThat(renamedTeamId).isPositive();

                renameTeam(adminCookie, renamedTeamId, "FairPlayCrew", "Waluigi", true);
                teamStrength.put("FairPlayCrew", teamStrength.remove("RudeName"));

                updateTournamentSettings(adminCookie, true, false, 6);
                return teamStrength;
        }

        private void subscribeAndNotify(MockCookie adminCookie, Map<String, Long> teamIds) throws Exception {
                for (String teamName : List.of("FSR", "Team-02")) {
                        mockMvc.perform(post("/public/notification/subscribe").with(csrf().asHeader())
                                        .contentType(APPLICATION_JSON)
                                        .content("""
                                                        {"endpoint":"https://push.example.com/%s","p256dh":"p256dh-key","auth":"auth-key","teamId":%d}
                                                        """.formatted(teamName, teamIds.get(teamName))))
                                        .andExpect(status().isOk());
                }

                mockMvc.perform(get("/admin/notification/subscriptions/count").cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value(2));

                clearInvocations(notificationSendService);
                mockMvc.perform(post("/admin/notification/send").with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"title":"Willkommen!","message":"Das Turnier beginnt."}
                                                """))
                                .andExpect(status().isOk());
                verify(notificationSendService, times(2)).sendNotification(any(PushSubscription.class), anyString());

                clearInvocations(notificationSendService);
                mockMvc.perform(post("/admin/notification/send/{teamId}", teamIds.get("FSR")).with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"title":"Bitte zur Anmeldung","message":"Kommt kurz zum Orga-Tisch."}
                                                """))
                                .andExpect(status().isOk());
                verify(notificationSendService, times(1)).sendNotification(any(PushSubscription.class), anyString());
        }

        private void createGroupSchedule(MockCookie adminCookie) throws Exception {
                clearInvocations(notificationSendService);
                MvcResult scheduleCreateResult = mockMvc
                                .perform(post("/admin/schedule/create/schedule").with(csrf().asHeader())
                                                .cookie(adminCookie)
                                                .contentType(APPLICATION_JSON)
                                                .content("""
                                                                {"version":2,"numFields":%d,"numRounds":%d,"teamsPerGame":%d}
                                                                """.formatted(SWITCH_COUNT, ROUND_COUNT, TEAMS_PER_GAME)))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode createdRounds = JSON.readTree(scheduleCreateResult.getResponse().getContentAsString());
                assertThat(createdRounds).hasSize(ROUND_COUNT);
                for (JsonNode round : getAdminRounds(adminCookie)) {
                        assertThat(round.path("games")).hasSize(SWITCH_COUNT);
                        int teamsInRound = 0;
                        for (JsonNode game : round.path("games")) {
                                assertThat(game.path("teams")).hasSize(TEAMS_PER_GAME);
                                teamsInRound += game.path("teams").size();
                        }
                        assertThat(teamsInRound).isEqualTo(TEAMS_PER_ROUND);
                }

                assertThat(getBreak(adminCookie).path("round").path("roundNumber").asInt()).isEqualTo(6);

                JsonNode overview = getOverview();
                assertThat(overview.path("scheduleCreated").asBoolean()).isTrue();
                assertThat(overview.path("finalScheduleCreated").asBoolean()).isFalse();
                assertThat(overview.path("numberOfRoundsUnplayed").asInt()).isEqualTo(ROUND_COUNT);
                assertThat(overview.path("tournamentOpen").asBoolean()).isTrue();
                long currentRoundId = overview.path("currentRounds").get(0).path("id").asLong();
                assertThat(overview.path("teamsNotInRound").path(String.valueOf(currentRoundId)))
                                .hasSize(TEAM_COUNT - TEAMS_PER_ROUND);

                verify(notificationSendService, timeout(5000).atLeast(2))
                                .sendNotification(any(PushSubscription.class), anyString());

                assertThat(getPublicGroupStandings()).hasSize(TEAM_COUNT);
        }

        private void answerGroupPhaseSurveys(MockCookie adminCookie, Map<String, Long> teamIds) throws Exception {
                List<Long> voters = new ArrayList<>(teamIds.values());

                long trackQuestion = createQuestion(adminCookie, "Welche Strecke fahrt ihr am liebsten?",
                                "MULTIPLE_CHOICE",
                                "[\"Regenbogen-Boulevard\",\"Toads Fabrik\",\"Wario-Stadion\",\"Kokos-Promenade\"]",
                                false, false);
                mockMvc.perform(get("/public/survey/visible"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].questionText").value("Welche Strecke fahrt ihr am liebsten?"));
                int[] trackVotes = { 0, 2, 0, 3, 0, 2 };
                for (int index = 0; index < trackVotes.length; index++) {
                        answer(voters.get(index), null, """
                                        {"questionId":%d,"answerType":"MULTIPLE_CHOICE","multipleChoiceSelectedOption":%d}
                                        """.formatted(trackQuestion, trackVotes[index]))
                                        .andExpect(status().isOk());
                }
                assertThat(statistics(adminCookie, trackQuestion)).containsExactly(3, 0, 2, 1);
                closeQuestion(adminCookie, trackQuestion, "Welche Strecke fahrt ihr am liebsten?", "MULTIPLE_CHOICE",
                                "[\"Regenbogen-Boulevard\",\"Toads Fabrik\",\"Wario-Stadion\",\"Kokos-Promenade\"]");
                answer(voters.get(6), null, """
                                {"questionId":%d,"answerType":"MULTIPLE_CHOICE","multipleChoiceSelectedOption":1}
                                """.formatted(trackQuestion))
                                .andExpect(status().isConflict());

                putSettings(adminCookie, """
                                {"surveyKeyMode":"DISTRIBUTING"}
                                """);
                long itemQuestion = createQuestion(adminCookie, "Welche Items mögt ihr?", "CHECKBOX",
                                "[\"Panzer\",\"Banane\",\"Pilz\",\"Blitz\"]", false, true);
                MvcResult firstPhone = answer(voters.get(0), null, """
                                {"questionId":%d,"answerType":"CHECKBOX","checkboxSelectedOptions":[0,2]}
                                """.formatted(itemQuestion))
                                .andExpect(status().isOk())
                                .andReturn();
                String surveyKey = firstPhone.getResponse().getCookie("surveyKey").getValue();
                assertThat(surveyKey).isNotBlank();
                answer(voters.get(1), surveyKey, """
                                {"questionId":%d,"answerType":"CHECKBOX","checkboxSelectedOptions":[1]}
                                """.formatted(itemQuestion))
                                .andExpect(status().isTooManyRequests());
                answer(voters.get(1), null, """
                                {"questionId":%d,"answerType":"CHECKBOX","checkboxSelectedOptions":[0,3]}
                                """.formatted(itemQuestion))
                                .andExpect(status().isOk());
                assertThat(statistics(adminCookie, itemQuestion)).containsExactly(2, 0, 1, 1);

                putSettings(adminCookie, """
                                {"surveyKeyMode":"REQUIRED"}
                                """);
                long feedbackQuestion = createQuestion(adminCookie, "Wie gefällt euch das Turnier?", "FREE_TEXT", "[]",
                                false, false);
                answer(voters.get(2), null, """
                                {"questionId":%d,"answerType":"FREE_TEXT","freeTextAnswer":"Ohne Schlüssel"}
                                """.formatted(feedbackQuestion))
                                .andExpect(status().isForbidden());
                answer(voters.get(2), surveyKey, """
                                {"questionId":%d,"answerType":"FREE_TEXT","freeTextAnswer":"Super Stimmung!"}
                                """.formatted(feedbackQuestion))
                                .andExpect(status().isOk());
                putSettings(adminCookie, """
                                {"surveyKeyMode":"DISABLED"}
                                """);
                JsonNode feedback = getJson(get("/admin/survey/{id}/answers", feedbackQuestion).cookie(adminCookie));
                assertThat(feedback).hasSize(1);
                assertThat(feedback.get(0).path("freeTextAnswer").asText()).isEqualTo("Super Stimmung!");

                long mottoQuestion = createQuestion(adminCookie, "Wie lautet euer Team-Motto?", "TEAM_ONE_FREE_TEXT",
                                "[]", false, false);
                for (int attempt = 0; attempt < 2; attempt++) {
                        answer(voters.get(3), null, """
                                        {"questionId":%d,"answerType":"TEAM_ONE_FREE_TEXT","freeTextAnswer":"Bananen voraus!","teamSelectedOption":%d}
                                        """.formatted(mottoQuestion, voters.get(3)))
                                        .andExpect(attempt == 0 ? status().isOk() : status().isTooManyRequests());
                }
                assertThat(getJson(get("/admin/survey/{id}/answers/count", mottoQuestion).cookie(adminCookie)).asInt())
                                .isEqualTo(1);
        }

        private void playGroupRounds(MockCookie adminCookie, Map<String, Integer> teamStrength) throws Exception {
                boolean breakMoved = false;
                boolean breakCompleted = false;
                boolean pointsCorrected = false;
                int playedGroupRounds = 0;

                while (getUnplayedRoundCount() > 0) {
                        JsonNode currentRounds = getCurrentRounds();
                        assertThat(currentRounds.isArray()).isTrue();
                        assertThat(currentRounds.size()).isBetween(1, 2);
                        assertThat(getPublicGroupStandings()).hasSize(TEAM_COUNT);

                        long currentRoundId = currentRounds.get(0).path("id").asLong();

                        if (playedGroupRounds == 5 && !breakMoved) {
                                assertThat(getBreak(adminCookie).path("round").path("id").asLong())
                                                .isEqualTo(currentRoundId);
                                JsonNode roundSeven = findRoundByRoundNumber(getAdminRounds(adminCookie), 7)
                                                .orElseThrow();
                                JsonNode movedBreak = updateBreak(adminCookie, roundSeven.path("id").asLong(), 30,
                                                false);
                                assertThat(movedBreak.path("round").path("roundNumber").asInt()).isEqualTo(7);
                                assertThat(getCurrentRounds().get(0).path("breakTime").isNull()
                                                || getCurrentRounds().get(0).path("breakTime").isMissingNode())
                                                .isTrue();
                                breakMoved = true;
                        }

                        JsonNode currentBreak = getBreak(adminCookie);
                        if (currentBreak.path("round").path("id").asLong() == currentRoundId
                                        && !currentBreak.path("breakEnded").asBoolean()) {
                                assertThat(currentBreak.path("round").path("roundNumber").asInt()).isEqualTo(7);
                                JsonNode completedBreak = updateBreak(adminCookie, currentRoundId, 30, true);
                                assertThat(completedBreak.path("breakEnded").asBoolean()).isTrue();
                                breakCompleted = true;
                        }

                        JsonNode roundDetails = getAdminRound(adminCookie, currentRoundId);
                        if (playedGroupRounds == 3) {
                                JsonNode mistypedPoints = roundDetails.path("games").get(0).path("points").get(0);
                                String mistypedTeam = mistypedPoints.path("team").path("teamName").asText();
                                Map<String, Integer> mistyped = new LinkedHashMap<>(teamStrength);
                                mistyped.put(mistypedTeam, 1);
                                applyScoresToRound(adminCookie, roundDetails, mistyped);
                                markRoundPlayed(adminCookie, currentRoundId);
                                int wrongPoints = groupPointsOf(adminCookie, mistypedTeam);

                                applyScoresToRound(adminCookie, getAdminRound(adminCookie, currentRoundId),
                                                teamStrength);
                                assertThat(groupPointsOf(adminCookie, mistypedTeam))
                                                .isEqualTo(wrongPoints - 1 + teamStrength.get(mistypedTeam));
                                pointsCorrected = true;
                        } else {
                                clearInvocations(notificationSendService);
                                applyScoresToRound(adminCookie, roundDetails, teamStrength);
                                markRoundPlayed(adminCookie, currentRoundId);
                                if (getUnplayedRoundCount() > 0) {
                                        verify(notificationSendService, timeout(5000).atLeast(2))
                                                        .sendNotification(any(PushSubscription.class), anyString());
                                }
                        }
                        playedGroupRounds++;

                        JsonNode updatedStandings = getPublicGroupStandings();
                        assertThat(updatedStandings).hasSize(TEAM_COUNT);
                        assertThat(teamNames(updatedStandings)).contains("FSR");
                        assertThat(getOverview().path("numberOfRoundsUnplayed").asInt())
                                        .isEqualTo(ROUND_COUNT - playedGroupRounds);

                        if (getUnplayedRoundCount() > 0) {
                                JsonNode nextCurrentRounds = getCurrentRounds();
                                assertThat(nextCurrentRounds.get(0).path("id").asLong()).isNotEqualTo(currentRoundId);
                        }
                }

                assertThat(playedGroupRounds).isEqualTo(ROUND_COUNT);
                assertThat(breakMoved).isTrue();
                assertThat(breakCompleted).isTrue();
                assertThat(pointsCorrected).isTrue();
        }

        private void checkGroupStandings(MockCookie adminCookie, Map<String, Integer> teamStrength) throws Exception {
                assertThat(teamNames(getPublicGroupStandings()).get(0)).isEqualTo("FSR");
                JsonNode standings = getFinalStandings(adminCookie);
                List<String> expectedOrder = teamStrength.entrySet().stream()
                                .sorted((first, second) -> second.getValue() - first.getValue())
                                .map(Map.Entry::getKey)
                                .toList();
                assertThat(teamNames(standings)).containsExactlyElementsOf(expectedOrder);
                for (JsonNode team : standings) {
                        int gamesPlayed = team.path("numberOfGamesPlayed").asInt();
                        assertThat(gamesPlayed).isPositive();
                        assertThat(team.path("groupPoints").asInt())
                                        .isEqualTo(teamStrength.get(team.path("teamName").asText()) * gamesPlayed);
                }
        }

        private void playFinal(MockCookie adminCookie, Map<String, Integer> teamStrength, Map<String, Long> teamIds)
                        throws Exception {
                JsonNode fsr = getPublicGroupStandings().get(0);
                assertThat(fsr.path("teamName").asText()).isEqualTo("FSR");
                renameTeam(adminCookie, fsr.path("id").asLong(), "FSR", "Toad", false);

                List<String> expectedFinalists = List.of("Team-02", "Team-03", "Team-04", "Team-05");
                JsonNode finalists = getFinalTeams(adminCookie);
                assertThat(teamNames(finalists)).containsExactlyElementsOf(expectedFinalists);

                clearInvocations(notificationSendService);
                mockMvc.perform(post("/admin/schedule/create/final_schedule").with(csrf().asHeader())
                                .cookie(adminCookie))
                                .andExpect(status().isOk());
                assertThat(getOverview().path("finalScheduleCreated").asBoolean()).isTrue();
                verify(notificationSendService, timeout(5000).atLeast(2))
                                .sendNotification(any(PushSubscription.class), anyString());

                long finalQuestion = createQuestion(adminCookie, "Wer gewinnt das Finale?", "TEAM", "[]", true, false);
                JsonNode question = findQuestion(adminCookie, finalQuestion);
                List<String> options = IntStream.range(0, question.path("options").size())
                                .mapToObj(index -> question.path("options").get(index).asText())
                                .toList();
                assertThat(options).containsExactlyInAnyOrderElementsOf(expectedFinalists);
                List<String> votes = List.of("Team-05", "Team-05", "Team-02", "Team-05", "Team-04");
                List<Long> voters = new ArrayList<>(teamIds.values());
                for (int index = 0; index < votes.size(); index++) {
                        answer(voters.get(index), null, """
                                        {"questionId":%d,"answerType":"TEAM","teamSelectedOption":%d}
                                        """.formatted(finalQuestion, options.indexOf(votes.get(index))))
                                        .andExpect(status().isOk());
                }
                List<Integer> expectedVotes = options.stream()
                                .map(option -> (int) votes.stream().filter(option::equals).count())
                                .toList();
                assertThat(statistics(adminCookie, finalQuestion)).containsExactlyElementsOf(expectedVotes);

                Map<String, Integer> finalStrength = Map.of("Team-02", 10, "Team-03", 8, "Team-04", 12, "Team-05", 15);
                int finalRounds = 0;
                while (getUnplayedRoundCount() > 0) {
                        long currentRoundId = getCurrentRounds().get(0).path("id").asLong();
                        JsonNode roundDetails = getAdminRound(adminCookie, currentRoundId);
                        assertThat(roundDetails.path("games")).hasSize(3);
                        applyScoresToRound(adminCookie, roundDetails, finalStrength);
                        markRoundPlayed(adminCookie, currentRoundId);
                        finalRounds++;
                        assertThat(getFinalStandings(adminCookie)).hasSize(TEAM_COUNT);
                }
                assertThat(finalRounds).isEqualTo(2);

                JsonNode finalStandings = getFinalStandings(adminCookie);
                assertThat(teamNames(finalStandings).subList(0, 4))
                                .containsExactly("Team-05", "Team-04", "Team-02", "Team-03");
                assertThat(finalStandings.get(0).path("finalPoints").asInt()).isEqualTo(6 * 15 + 1);
                assertThat(finalStandings.get(4).path("teamName").asText()).isEqualTo("FSR");
                assertThat(finalStandings.get(4).path("finalPoints").asInt()).isZero();
                assertThat(teamStrength).containsKey("FSR");
        }

        private void resetApplication(MockCookie adminCookie) throws Exception {
                mockMvc.perform(delete("/admin/settings/reset").with(csrf().asHeader()).cookie(adminCookie))
                                .andExpect(status().isOk());

                assertThat(teamRepository.count()).isZero();
                assertThat(roundRepository.count()).isZero();
                assertThat(questionRepository.count()).isZero();
                assertThat(getOverview().path("scheduleCreated").asBoolean()).isFalse();
        }

        private void putSettings(MockCookie adminCookie, String body) throws Exception {
                mockMvc.perform(put("/admin/settings").with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk());
        }

        private void updateTournamentSettings(MockCookie adminCookie, boolean tournamentOpen, boolean registrationOpen,
                        int maxGamesCount)
                        throws Exception {
                putSettings(adminCookie, """
                                {"tournamentOpen":%s,"registrationOpen":%s,"maxGamesCount":%d}
                                """.formatted(tournamentOpen, registrationOpen, maxGamesCount));
        }

        private long registerTeam(String teamName, String characterName) throws Exception {
                MvcResult result = mockMvc.perform(post("/public/teams").with(csrf().asHeader())
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"teamName":"%s","characterName":"%s","finalReady":true,"active":true}
                                                """.formatted(teamName, characterName)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.teamName").value(teamName))
                                .andReturn();

                return jsonFieldAsLong(result, "id");
        }

        private void renameTeam(MockCookie adminCookie, long teamId, String newName, String characterName,
                        boolean finalReady)
                        throws Exception {
                mockMvc.perform(put("/admin/teams/{id}", teamId).with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"teamName":"%s","characterName":"%s","finalReady":%s,"active":true}
                                                """.formatted(newName, characterName, finalReady)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.teamName").value(newName))
                                .andExpect(jsonPath("$.finalReady").value(finalReady));
        }

        private List<String> availableCharacterNames() throws Exception {
                JsonNode characters = getJson(get("/public/teams/characters/available"));
                return IntStream.range(0, characters.size())
                                .mapToObj(index -> characters.get(index).path("characterName").asText())
                                .toList();
        }

        private Map<String, Long> teamIdsByName() throws Exception {
                Map<String, Long> teamIds = new LinkedHashMap<>();
                for (JsonNode team : getJson(get("/public/teams/sortedByTeamName"))) {
                        teamIds.put(team.path("teamName").asText(), team.path("id").asLong());
                }
                assertThat(teamIds).hasSize(TEAM_COUNT);
                return teamIds;
        }

        private long createQuestion(MockCookie adminCookie, String questionText, String questionType, String options,
                        boolean finalTeamsOnly, boolean oneAnswerPerKey) throws Exception {
                MvcResult result = mockMvc.perform(post("/admin/survey").with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content(questionBody(questionText, questionType, options, true, finalTeamsOnly,
                                                oneAnswerPerKey)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.questionText").value(questionText))
                                .andReturn();
                return jsonFieldAsLong(result, "id");
        }

        private void closeQuestion(MockCookie adminCookie, long questionId, String questionText, String questionType,
                        String options) throws Exception {
                mockMvc.perform(put("/admin/survey/{id}", questionId).with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content(questionBody(questionText, questionType, options, false, false, false)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.active").value(false));
        }

        private String questionBody(String questionText, String questionType, String options, boolean active,
                        boolean finalTeamsOnly, boolean oneAnswerPerKey) {
                return """
                                {"questionText":"%s","questionType":"%s","options":%s,"active":%s,"visible":true,"live":false,"finalTeamsOnly":%s,"oneAnswerPerKey":%s}
                                """.formatted(questionText, questionType, options, active, finalTeamsOnly,
                                oneAnswerPerKey);
        }

        private ResultActions answer(long teamId, String surveyKey, String body)
                        throws Exception {
                var request = post("/public/survey/answer").with(csrf().asHeader())
                                .cookie(new MockCookie("user", "{\"teamId\":%d}".formatted(teamId)))
                                .contentType(APPLICATION_JSON)
                                .content(body);
                if (surveyKey != null) {
                        request.cookie(new MockCookie("surveyKey", surveyKey));
                }
                return mockMvc.perform(request);
        }

        private List<Integer> statistics(MockCookie adminCookie, long questionId) throws Exception {
                JsonNode statistics = getJson(get("/admin/survey/{id}/statistics", questionId).cookie(adminCookie));
                return IntStream.range(0, statistics.size())
                                .mapToObj(index -> statistics.get(index).asInt())
                                .toList();
        }

        private JsonNode findQuestion(MockCookie adminCookie, long questionId) throws Exception {
                for (JsonNode question : getJson(get("/admin/survey").cookie(adminCookie))) {
                        if (question.path("id").asLong() == questionId) {
                                return question;
                        }
                }
                throw new AssertionError("Question %d not found".formatted(questionId));
        }

        private int groupPointsOf(MockCookie adminCookie, String teamName) throws Exception {
                for (JsonNode team : getFinalStandings(adminCookie)) {
                        if (team.path("teamName").asText().equals(teamName)) {
                                return team.path("groupPoints").asInt();
                        }
                }
                throw new AssertionError("Team %s not found".formatted(teamName));
        }

        private JsonNode getJson(RequestBuilder request) throws Exception {
                MvcResult result = mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andReturn();
                return JSON.readTree(result.getResponse().getContentAsString());
        }

        private JsonNode getOverview() throws Exception {
                return getJson(get("/public/schedule/overview"));
        }

        private JsonNode getCurrentRounds() throws Exception {
                return getJson(get("/public/schedule/rounds/current"));
        }

        private int getUnplayedRoundCount() throws Exception {
                return getJson(get("/public/schedule/rounds/unplayed")).asInt();
        }

        private JsonNode getPublicGroupStandings() throws Exception {
                return getJson(get("/public/teams/sortedByGroupPoints"));
        }

        private JsonNode getAdminRounds(MockCookie adminCookie) throws Exception {
                return getJson(get("/admin/schedule/rounds").cookie(adminCookie));
        }

        private JsonNode getAdminRound(MockCookie adminCookie, long roundId) throws Exception {
                return getJson(get("/admin/schedule/rounds/{roundId}", roundId).cookie(adminCookie));
        }

        private JsonNode getBreak(MockCookie adminCookie) throws Exception {
                return getJson(get("/admin/schedule/break").cookie(adminCookie));
        }

        private JsonNode updateBreak(MockCookie adminCookie, long roundId, int duration, boolean breakEnded)
                        throws Exception {
                MvcResult result = mockMvc.perform(put("/admin/schedule/break").with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"roundId":%d,"breakDuration":%d,"breakEnded":%s}
                                                """.formatted(roundId, duration, breakEnded)))
                                .andExpect(status().isOk())
                                .andReturn();
                return JSON.readTree(result.getResponse().getContentAsString());
        }

        private void applyScoresToRound(MockCookie adminCookie, JsonNode roundDetails,
                        Map<String, Integer> teamStrength)
                        throws Exception {
                for (JsonNode gameNode : roundDetails.path("games")) {
                        long gameId = gameNode.path("id").asLong();
                        for (JsonNode pointsNode : gameNode.path("points")) {
                                long teamId = pointsNode.path("team").path("id").asLong();
                                String teamName = pointsNode.path("team").path("teamName").asText();
                                int score = teamStrength.getOrDefault(teamName, 1);

                                mockMvc.perform(put(
                                                "/admin/schedule/rounds/{roundId}/games/{gameId}/teams/{teamId}/points",
                                                roundDetails.path("id").asLong(), gameId, teamId)
                                                .cookie(adminCookie)
                                                .with(csrf().asHeader())
                                                .contentType(APPLICATION_JSON)
                                                .content("""
                                                                {"points":%d}
                                                                """.formatted(score)))
                                                .andExpect(status().isOk());
                        }
                }
        }

        private void markRoundPlayed(MockCookie adminCookie, long roundId) throws Exception {
                mockMvc.perform(put("/admin/schedule/rounds/{roundId}", roundId).with(csrf().asHeader())
                                .cookie(adminCookie)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {"played":true}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.played").value(true));
        }

        private JsonNode getFinalTeams(MockCookie adminCookie) throws Exception {
                return getJson(get("/admin/teams/finalTeams").cookie(adminCookie));
        }

        private JsonNode getFinalStandings(MockCookie adminCookie) throws Exception {
                return getJson(get("/admin/teams/sortedByFinalPoints").cookie(adminCookie));
        }

        private List<String> teamNames(JsonNode teams) {
                return IntStream.range(0, teams.size())
                                .mapToObj(index -> teams.get(index).path("teamName").asText())
                                .toList();
        }

        private Optional<JsonNode> findRoundByRoundNumber(JsonNode rounds, int roundNumber) {
                return IntStream.range(0, rounds.size())
                                .mapToObj(rounds::get)
                                .filter(round -> round.path("roundNumber").asInt() == roundNumber)
                                .findFirst();
        }
}
