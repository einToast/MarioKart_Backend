package de.fsr.mariokart_backend.match_plan.model.dto;

import de.fsr.mariokart_backend.registration.model.dto.TeamFromGameReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameReturnDTO {
    private Long id;
    private String switchGame;
    private RoundFromGameReturnDTO round;
    private Set<TeamFromGameReturnDTO> teams;
    private Set<PointsFromGameReturnDTO> points;
}
