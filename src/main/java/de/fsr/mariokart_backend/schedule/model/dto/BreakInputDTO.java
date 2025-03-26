package de.fsr.mariokart_backend.schedule.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BreakInputDTO {
    private Long roundId;
    private int breakDuration;
    private Boolean breakEnded;
}
