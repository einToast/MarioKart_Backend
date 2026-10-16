package de.fsr.mariokart_backend.registration.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;

@Tag("unit")
class TeamTest {

    @Test
    void getGroupPointsSortsByIdWithinSameRoundAndLimitsByMaxGames() {
        Team team = new Team();

        Points points1 = points(3L, team, 10, 1, playedRound(true));
        Points points2 = points(1L, team, 6, 2, playedRound(true));
        Points points3 = points(2L, team, 7, 3, playedRound(true));

        team.setPoints(new HashSet<>(Set.of(points1, points2, points3)));

        assertThat(team.getGroupPoints(2)).isEqualTo(13);
    }

    @Test
    void getGroupPointsSortsByRoundNumberBeforeId() {
        Team team = new Team();

        Points laterRound = points(1L, team, 100, 0, numberedRound(2));
        Points earlierRound = points(2L, team, 5, 0, numberedRound(1));

        team.setPoints(new HashSet<>(Set.of(laterRound, earlierRound)));

        assertThat(team.getGroupPoints(1)).isEqualTo(5);
    }

    @Test
    void getAverageGroupPointsAveragesOnlyCountedGames() {
        Team team = new Team();

        team.setPoints(new HashSet<>(Set.of(
                points(1L, team, 10, 0, numberedRound(1)),
                points(2L, team, 20, 0, numberedRound(2)),
                points(3L, team, 90, 0, numberedRound(3)))));

        assertThat(team.getAverageGroupPoints(2)).isEqualTo(15.0);
    }

    @Test
    void getAverageGroupPointsUsesPlayedGamesSoFar() {
        Team team = new Team();

        Round unplayed = playedRound(false);
        unplayed.setRoundNumber(2);

        team.setPoints(new HashSet<>(Set.of(
                points(1L, team, 12, 0, numberedRound(1)),
                points(2L, team, 0, 0, unplayed))));

        assertThat(team.getAverageGroupPoints(6)).isEqualTo(12.0);
    }

    @Test
    void getAverageGroupPointsReturnsZeroWithoutPlayedGames() {
        Team team = new Team();

        assertThat(team.getAverageGroupPoints(6)).isZero();
    }

    @Test
    void getAverageGroupPointsOfAllGamesIgnoresUnplayedAndFinalRounds() {
        Team team = new Team();

        Round finalRound = playedRound(true);
        finalRound.setFinalGame(true);

        team.setPoints(new HashSet<>(Set.of(
                points(1L, team, 10, 0, playedRound(true)),
                points(2L, team, 20, 0, playedRound(true)),
                points(3L, team, 30, 0, playedRound(true)),
                points(4L, team, 0, 0, playedRound(false)),
                points(5L, team, 0, 50, finalRound))));

        assertThat(team.getAverageGroupPointsOfAllGames()).isEqualTo(20.0);
    }

    @Test
    void getAverageGroupPointsOfAllGamesReturnsZeroWithoutPlayedGames() {
        Team team = new Team();
        assertThat(team.getAverageGroupPointsOfAllGames()).isZero();

        team.setPoints(new HashSet<>(Set.of(points(1L, team, 0, 0, playedRound(false)))));
        assertThat(team.getAverageGroupPointsOfAllGames()).isZero();
    }

    @Test
    void getGroupPointsReturnsZeroWhenPointsMissing() {
        Team team = new Team();

        assertThat(team.getGroupPoints(10)).isZero();
    }

    @Test
    void getFinalPointsSumsAllFinalPoints() {
        Team team = new Team();
        team.setPoints(new HashSet<>(Set.of(
                points(1L, team, 1, 5, playedRound(true)),
                points(2L, team, 2, 9, playedRound(true)))));

        assertThat(team.getFinalPoints()).isEqualTo(14);
    }

    @Test
    void getGamesReturnsUniqueGameSet() {
        Team team = new Team();
        Round round = playedRound(true);
        Game game = game(1L, round);

        Points p1 = points(1L, team, 0, 0, round);
        p1.setGame(game);
        Points p2 = points(2L, team, 0, 0, round);
        p2.setGame(game);

        team.setPoints(new HashSet<>(Set.of(p1, p2)));

        assertThat(team.getGames()).hasSize(1);
        assertThat(team.getGames()).contains(game);
    }

    @Test
    void getNumberOfGamesPlayedCountsPlayedAndCapsAtMaxGames() {
        Team team = new Team();

        Points played1 = points(1L, team, 0, 0, playedRound(true));
        Points played2 = points(2L, team, 0, 0, playedRound(true));
        Points unplayed = points(3L, team, 0, 0, playedRound(false));

        team.setPoints(new HashSet<>(Set.of(played1, played2, unplayed)));

        assertThat(team.getNumberOfGamesPlayed(1)).isEqualTo(1);
        assertThat(team.getNumberOfGamesPlayed(5)).isEqualTo(2);
    }

    @Test
    void removeCharacterDetachesCharacterBothWays() {
        Team team = new Team();
        Character character = new Character();
        character.setCharacterName("Mario");
        character.setTeam(team);
        team.setCharacter(character);

        team.removeCharacter();

        assertThat(team.getCharacter()).isNull();
        assertThat(character.getTeam()).isNull();
    }

    @Test
    void removeTeamAssociationsClearsQuestionsAndCharacter() {
        Team team = new Team();

        Character character = new Character();
        character.setCharacterName("Luigi");
        character.setTeam(team);
        team.setCharacter(character);

        TeamQuestion question = new TeamQuestion();
        question.setTeams(new ArrayList<>(List.of(team)));
        team.setTeamQuestions(new HashSet<>(Set.of(question)));

        team.removeTeamAssociations();

        assertThat(question.getTeams()).doesNotContain(team);
        assertThat(team.getTeamQuestions()).isEmpty();
        assertThat(team.getCharacter()).isNull();
        assertThat(character.getTeam()).isNull();
    }

    private Round playedRound(boolean played) {
        Round round = new Round();
        round.setPlayed(played);
        round.setStartTime(LocalDateTime.now());
        round.setEndTime(LocalDateTime.now().plusMinutes(20));
        return round;
    }

    private Round numberedRound(int roundNumber) {
        Round round = playedRound(true);
        round.setRoundNumber(roundNumber);
        return round;
    }

    private Game game(Long id, Round round) {
        Game game = new Game();
        game.setId(id);
        game.setRound(round);
        return game;
    }

    private Points points(Long id, Team team, int groupPoints, int finalPoints, Round round) {
        Points points = new Points();
        points.setId(id);
        points.setTeam(team);
        points.setGroupPoints(groupPoints);
        points.setFinalPoints(finalPoints);
        points.setGame(game(id, round));
        return points;
    }
}
