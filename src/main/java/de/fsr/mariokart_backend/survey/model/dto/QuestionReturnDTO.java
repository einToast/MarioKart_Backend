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
public class QuestionReturnDTO {
    private Long id;
    private String questionType;
    private String questionText;
    private List<String> options;
    private boolean active;
    private boolean visible;
    private boolean live;
    private boolean finalTeamsOnly;
    private boolean oneAnswerPerKey;
    private Map<String, QuestionTranslation> translations;
}
