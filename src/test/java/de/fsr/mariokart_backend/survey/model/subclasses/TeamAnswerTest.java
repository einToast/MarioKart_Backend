package de.fsr.mariokart_backend.survey.model.subclasses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.registration.model.Team;

@Tag("unit")
class TeamAnswerTest {

    @Test
    void getAnswerDetailsReturnsTeamName() {
        Team team = new Team();
        team.setTeamName("Blue Shells");

        TeamAnswer answer = new TeamAnswer();
        answer.setTeam(team);

        assertThat(answer.getAnswerDetails()).isEqualTo("Team: Blue Shells");
    }
}
