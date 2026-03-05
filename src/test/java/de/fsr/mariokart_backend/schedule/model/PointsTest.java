package de.fsr.mariokart_backend.schedule.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.registration.model.Team;

@Tag("unit")
class PointsTest {

    @Test
    void toStringReturnsGameAndTeamIds() {
        Team team = new Team();
        team.setId(4L);

        Game game = new Game();
        game.setId(9L);

        Points points = new Points();
        points.setTeam(team);
        points.setGame(game);

        assertThat(points.toString()).isEqualTo("9 4");
    }
}
