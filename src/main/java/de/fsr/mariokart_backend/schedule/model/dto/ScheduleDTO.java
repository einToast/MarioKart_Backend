package de.fsr.mariokart_backend.schedule.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDTO {
    @JsonProperty("max_games_count")
    private int maxGamesCount;
    private List<List<List<Integer>>> plan;
}
