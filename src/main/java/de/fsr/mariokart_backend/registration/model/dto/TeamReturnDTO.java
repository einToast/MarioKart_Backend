package de.fsr.mariokart_backend.registration.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamReturnDTO {
    private Long id;
    private String teamName;
    private CharacterReturnDTO character;
    private boolean finalReady;
    private boolean active;
    private int groupPoints;
    private int finalPoints;
    private int numberOfGamesPlayed;
}
