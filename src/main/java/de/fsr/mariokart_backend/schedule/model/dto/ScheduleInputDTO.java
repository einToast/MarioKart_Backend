package de.fsr.mariokart_backend.schedule.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleInputDTO {
    private int version;
    private int numFields;
    private int numRounds;
    private int teamsPerGame;
}
