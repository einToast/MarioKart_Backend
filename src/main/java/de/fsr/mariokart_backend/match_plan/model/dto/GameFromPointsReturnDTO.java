package de.fsr.mariokart_backend.match_plan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameFromPointsReturnDTO {
    private Long id;
    private String switchGame;
    private RoundFromGameReturnDTO round;
}
