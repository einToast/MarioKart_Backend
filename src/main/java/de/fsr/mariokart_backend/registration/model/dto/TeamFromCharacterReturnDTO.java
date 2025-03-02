package de.fsr.mariokart_backend.registration.model.dto;

import java.util.Set;

import de.fsr.mariokart_backend.match_plan.model.dto.GameFromTeamReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamFromCharacterReturnDTO {
    private Long id;
    private String teamName;
    private boolean finalReady;
    private boolean active;
    private int groupPoints;
    private int finalPoints;
    private Set<GameFromTeamReturnDTO> games;
}
