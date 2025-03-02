package de.fsr.mariokart_backend.match_plan.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BreakReturnDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean breakEnded;
    private RoundFromBreakReturnDTO round;
}
