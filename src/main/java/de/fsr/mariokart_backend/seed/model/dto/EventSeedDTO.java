package de.fsr.mariokart_backend.seed.model.dto;

import java.util.List;

import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.ScheduleInputDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventSeedDTO {
    private TournamentDTO settings;
    private JsonNode floorPlan;
    private JsonNode program;
    private List<TeamInputDTO> teams;
    private List<QuestionInputDTO> survey;
    private ScheduleInputDTO schedule;
}
