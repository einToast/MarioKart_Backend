package de.fsr.mariokart_backend.match_plan.model.dto;

import java.time.LocalDateTime;

import de.fsr.mariokart_backend.match_plan.model.Break;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoundFromGameReturnDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean finalGame;
    private boolean played;
    private Break breakTime;
}
