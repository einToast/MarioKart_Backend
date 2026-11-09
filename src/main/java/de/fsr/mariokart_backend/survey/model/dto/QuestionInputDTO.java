package de.fsr.mariokart_backend.survey.model.dto;

import java.util.List;
import java.util.Map;

import de.fsr.mariokart_backend.survey.model.QuestionTranslation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionInputDTO {
    private String questionText;
    private String questionType;
    private List<String> options;
    private boolean active;
    private boolean visible;
    private boolean live;
    private boolean finalTeamsOnly;
    private boolean oneAnswerPerKey;
    private Map<String, QuestionTranslation> translations;

    public QuestionInputDTO(String questionText, String questionType, List<String> options, boolean active,
            boolean visible, boolean live, boolean finalTeamsOnly, boolean oneAnswerPerKey) {
        this(questionText, questionType, options, active, visible, live, finalTeamsOnly, oneAnswerPerKey, null);
    }
}
