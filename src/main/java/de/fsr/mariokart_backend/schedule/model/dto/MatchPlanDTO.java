package de.fsr.mariokart_backend.schedule.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlanDTO {
    private int max_games_count;
    private List<List<List<Integer>>> plan;
}
