package de.fsr.mariokart_backend.survey.model.subclasses;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.survey.model.Answer;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("TEAM_ONE_FREE_TEXT")
public class TeamOneFreeTextAnswer extends Answer {
    private String textAnswer;

    @OneToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Override
    public String getAnswerDetails() {
        return "Text response: " + textAnswer + ", Team: " + (team != null ? team.getTeamName() : "No team");
    }
}
