package de.fsr.mariokart_backend.registration.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamFromGameReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamFromPointsReturnDTO;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RegistrationFromMatchPlanReturnDTOService {

    private final RegistrationReturnDTOService registrationReturnDTOService;
    private final SettingsService settingsService;

    public TeamFromGameReturnDTO teamToTeamFromGameReturnDTO(Team team) {
        if (team == null)
            return null;
        return new TeamFromGameReturnDTO(team.getId(), team.getTeamName(),
                registrationReturnDTOService.characterToCharacterFromTeamReturnDTO(team.getCharacter()),
                team.isFinalReady(), team.isActive(),
                team.getGroupPoints(settingsService.getSettings().getMaxGamesCount()), team.getFinalPoints());
    }

    public TeamFromPointsReturnDTO teamToTeamFromPointsReturnDTO(Team team) {
        if (team == null)
            return null;
        return new TeamFromPointsReturnDTO(team.getId(), team.getTeamName(),
                registrationReturnDTOService.characterToCharacterFromTeamReturnDTO(team.getCharacter()),
                team.isFinalReady(), team.isActive(),
                team.getGroupPoints(settingsService.getSettings().getMaxGamesCount()), team.getFinalPoints());
    }
}
