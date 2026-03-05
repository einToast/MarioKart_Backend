package de.fsr.mariokart_backend.survey.model.subclasses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.registration.model.Team;

@Tag("unit")
class TeamOneFreeTextAnswerTest {

    @Test
    void getAnswerDetailsReturnsTextAndTeamWhenTeamExists() {
        Team team = new Team();
        team.setTeamName("Fire Flowers");

        TeamOneFreeTextAnswer answer = new TeamOneFreeTextAnswer();
        answer.setTextAnswer("My final pick");
        answer.setTeam(team);

        assertThat(answer.getAnswerDetails())
                .isEqualTo("Text response: My final pick, Team: Fire Flowers");
    }

    @Test
    void getAnswerDetailsReturnsFallbackWhenTeamMissing() {
        TeamOneFreeTextAnswer answer = new TeamOneFreeTextAnswer();
        answer.setTextAnswer("No team set");

        assertThat(answer.getAnswerDetails())
                .isEqualTo("Text response: No team set, Team: No team");
    }
}
