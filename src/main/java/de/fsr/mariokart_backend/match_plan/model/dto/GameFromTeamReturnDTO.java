package de.fsr.mariokart_backend.match_plan.model.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameFromTeamReturnDTO {
    private Long id;
    private String switchGame;
    private RoundFromGameFromTeamReturnDTO round;
    private Set<PointsFromGameFromTeamReturnDTO> points;
}
