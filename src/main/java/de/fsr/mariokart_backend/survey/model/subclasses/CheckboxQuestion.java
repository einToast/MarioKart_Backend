package de.fsr.mariokart_backend.survey.model.subclasses;

import java.util.List;

import de.fsr.mariokart_backend.survey.model.Question;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("CHECKBOX")
public class CheckboxQuestion extends Question {
    @ElementCollection
    private List<String> options;
}
