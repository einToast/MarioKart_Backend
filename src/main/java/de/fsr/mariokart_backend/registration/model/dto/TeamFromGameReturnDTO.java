package de.fsr.mariokart_backend.registration.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamFromGameReturnDTO {
    private Long id;
    private String teamName;
    private CharacterFromTeamReturnDTO character;
    private boolean finalReady;
    private int groupPoints;
    private int finalPoints;
}
