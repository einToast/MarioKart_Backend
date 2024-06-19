package de.fsr.mariokart_backend.registration.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CharacterReturnDTO {
    private Long id;
    private String characterName;
    private TeamFromCharacterReturnDTO team;
}
