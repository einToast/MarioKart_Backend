package de.fsr.mariokart_backend.match_plan.model.dto;

import de.fsr.mariokart_backend.registration.model.dto.TeamFromPointsReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PointsFromGameFromTeamReturnDTO {
    private Long id;
    private int points;
}
