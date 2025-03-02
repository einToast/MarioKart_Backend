package de.fsr.mariokart_backend.match_plan.model.dto;

import java.time.LocalDateTime;
import java.util.Set;

import de.fsr.mariokart_backend.match_plan.model.Break;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoundReturnDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean finalGame;
    private boolean played;
    private Set<GameFromRoundReturnDTO> games;
    private Break breakTime;
}
