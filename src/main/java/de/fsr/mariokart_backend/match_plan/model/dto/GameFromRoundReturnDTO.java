package de.fsr.mariokart_backend.match_plan.model.dto;

import java.util.Set;

import de.fsr.mariokart_backend.registration.model.dto.TeamFromGameReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameFromRoundReturnDTO {
    private Long id;
    private String switchGame;
    private Set<TeamFromGameReturnDTO> teams;
    private Set<PointsFromGameReturnDTO> points;
}
