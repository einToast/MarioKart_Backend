package de.fsr.mariokart_backend.schedule.service.pub;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationReadService;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.ScheduleOverviewDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicScheduleOverviewService {

    private final PublicScheduleReadService publicScheduleReadService;
    private final PublicRegistrationReadService publicRegistrationReadService;
    private final PublicSettingsReadService publicSettingsReadService;

    public ScheduleOverviewDTO getOverview() {
        List<RoundReturnDTO> currentRounds = publicScheduleReadService.getCurrentRounds();

        Map<Long, List<TeamReturnDTO>> teamsNotInRound = new LinkedHashMap<>();
        for (RoundReturnDTO round : currentRounds) {
            teamsNotInRound.put(round.getId(), publicRegistrationReadService.getTeamsNotInRound(round.getId()));
        }

        return new ScheduleOverviewDTO(currentRounds, teamsNotInRound,
                publicScheduleReadService.isScheduleCreated(),
                publicScheduleReadService.isFinalScheduleCreated(),
                publicScheduleReadService.getNumberOfRoundsUnplayed(),
                Boolean.TRUE.equals(publicSettingsReadService.getSettings().getTournamentOpen()));
    }
}
