package de.fsr.mariokart_backend.schedule.model.dto;

import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsInputFullDTO {
    private int points;
    private TeamInputDTO team;
}
