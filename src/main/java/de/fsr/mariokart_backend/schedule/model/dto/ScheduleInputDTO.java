package de.fsr.mariokart_backend.schedule.model.dto;

import java.util.List;

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
    private List<Integer> teamsPerField;

    public ScheduleInputDTO(int version, int numFields, int numRounds, int teamsPerGame) {
        this(version, numFields, numRounds, teamsPerGame, null);
    }
}
