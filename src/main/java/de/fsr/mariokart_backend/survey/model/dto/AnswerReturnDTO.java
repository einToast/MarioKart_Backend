package de.fsr.mariokart_backend.survey.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnswerReturnDTO {
    private Long questionId;
    private String answerType;
    private String freeTextAnswer;
    private Integer multipleChoiceSelectedOption;
    private List<Integer> checkboxSelectedOptions;
}
