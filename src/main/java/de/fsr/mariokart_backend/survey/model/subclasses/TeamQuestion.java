package de.fsr.mariokart_backend.survey.model.subclasses;

import java.util.List;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.survey.model.Question;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
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
public class TeamQuestion extends Question {

    @ManyToMany
    private List<Team> teams;

    private Boolean finalTeamsOnly;
}
