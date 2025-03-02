package de.fsr.mariokart_backend.survey.model.dto;

import java.util.List;

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
}
