package de.fsr.mariokart_backend.schedule.model.dto;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PointsReturnDTO {
    private Long id;
    private int points;
    private TeamReturnDTO team;
}
