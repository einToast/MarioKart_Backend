package de.fsr.mariokart_backend.schedule.model.dto;

import java.util.List;
import java.util.Map;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduleOverviewDTO {
    private List<RoundReturnDTO> currentRounds;
    private Map<Long, List<TeamReturnDTO>> teamsNotInRound;
    private boolean scheduleCreated;
    private boolean finalScheduleCreated;
    private int numberOfRoundsUnplayed;
    private boolean tournamentOpen;
}
