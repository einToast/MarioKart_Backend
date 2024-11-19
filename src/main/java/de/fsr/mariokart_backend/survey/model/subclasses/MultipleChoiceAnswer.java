package de.fsr.mariokart_backend.survey.model.subclasses;

import de.fsr.mariokart_backend.survey.model.Answer;
import jakarta.persistence.DiscriminatorValue;
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
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceAnswer extends Answer {
    private Integer selectedOption;

    @Override
    public String getAnswerDetails() {
        return "Selected option: " + selectedOption;
    }

}
