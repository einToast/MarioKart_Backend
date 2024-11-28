package de.fsr.mariokart_backend.survey.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionInputDTO {
    private String questionText;
    private String questionType;
    private List<String> options;
    private boolean active;
    private boolean live;
}
