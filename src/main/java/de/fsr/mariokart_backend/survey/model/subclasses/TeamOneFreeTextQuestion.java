package de.fsr.mariokart_backend.survey.model.subclasses;

import de.fsr.mariokart_backend.survey.model.Question;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@DiscriminatorValue("TEAM_ONE_FREE_TEXT")
public class TeamOneFreeTextQuestion extends Question {
}
