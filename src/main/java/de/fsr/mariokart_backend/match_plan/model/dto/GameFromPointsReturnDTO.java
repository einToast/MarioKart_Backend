package de.fsr.mariokart_backend.match_plan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameFromPointsReturnDTO {
    private Long id;
    private String switchGame;
    private RoundFromGameReturnDTO round;
}
