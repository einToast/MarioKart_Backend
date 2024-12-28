package de.fsr.mariokart_backend.registration.model.dto;

import de.fsr.mariokart_backend.match_plan.model.dto.GameFromTeamReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamReturnDTO {
    private Long id;
    private String teamName;
    private CharacterFromTeamReturnDTO character;
    private boolean finalReady;
    private boolean active;
    private int groupPoints;
    private int finalPoints;
    private Set<GameFromTeamReturnDTO> games;
}
