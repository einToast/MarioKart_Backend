package de.fsr.mariokart_backend.survey.model.subclasses;


import de.fsr.mariokart_backend.survey.model.Answer;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("CHECKBOX")
public class CheckboxAnswer extends Answer {
    @ElementCollection
    private List<Integer> selectedOptions;

    @Override
    public String getAnswerDetails() {
        return "Selected options: " + selectedOptions.stream()
                                                     .map(String::valueOf)
                                                     .collect(Collectors.joining(", "));
    }
}

