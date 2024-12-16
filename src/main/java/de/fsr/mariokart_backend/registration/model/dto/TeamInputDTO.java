package de.fsr.mariokart_backend.registration.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamInputDTO {
    private String teamName;
    private String characterName;
    private boolean finalReady;
    private boolean active;

    public TeamInputDTO(String teamName, String characterName) {
        this.teamName = teamName;
        this.characterName = characterName;
        this.finalReady = true;
        this.active = true;
    }
}
