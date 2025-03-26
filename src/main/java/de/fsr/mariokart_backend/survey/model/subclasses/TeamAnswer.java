package de.fsr.mariokart_backend.survey.model.subclasses;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.survey.model.Answer;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("TEAMQ")
public class TeamAnswer extends Answer {

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Override
    public String getAnswerDetails() {
        return "Team: " + team.getTeamName();
    }

}
