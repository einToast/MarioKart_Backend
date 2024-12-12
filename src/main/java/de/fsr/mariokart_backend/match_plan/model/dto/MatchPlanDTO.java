package de.fsr.mariokart_backend.match_plan.model.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlanDTO {
    private int max_games_count;
    private List<List<List<Integer>>> plan;


}
