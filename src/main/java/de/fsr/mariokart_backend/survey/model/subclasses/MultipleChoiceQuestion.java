package de.fsr.mariokart_backend.survey.model.subclasses;

import de.fsr.mariokart_backend.survey.model.Question;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceQuestion extends Question {
    @ElementCollection
    private List<String> options;
}

