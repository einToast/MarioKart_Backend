package de.fsr.mariokart_backend.schedule.model.dto;

import java.util.Set;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameReturnDTO {
    private Long id;
    private String switchGame;
    private Set<TeamReturnDTO> teams;
    private Set<PointsReturnDTO> points;
}
