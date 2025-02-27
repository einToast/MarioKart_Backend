package de.fsr.mariokart_backend.registration.service.dto;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.match_plan.service.dto.MatchPlanFromRegistrationReturnDTOService;
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterFromTeamReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamFromCharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RegistrationReturnDTOService {

    private final MatchPlanFromRegistrationReturnDTOService matchPlanFromRegistrationReturnDTOService;
    private final SettingsService settingsService;

    public CharacterReturnDTO characterToCharacterReturnDTO(Character character) {
        if (character == null)
            return null;
        return new CharacterReturnDTO(character.getId(), character.getCharacterName(),
                teamToTeamFromCharacterReturnDTO(character.getTeam()));
    }

    public TeamReturnDTO teamToTeamReturnDTO(Team team) {
        if (team == null)
            return null;
        return new TeamReturnDTO(team.getId(), team.getTeamName(),
                characterToCharacterFromTeamReturnDTO(team.getCharacter()), team.isFinalReady(), team.isActive(),
                team.getGroupPoints(settingsService.getSettings().getMaxGamesCount()), team.getFinalPoints(),
                team.getGames() != null ? team.getGames().stream()
                        .map(matchPlanFromRegistrationReturnDTOService::gameToGameFromTeamReturnDTO)
                        .collect(Collectors.toSet()) : null);
    }

    public CharacterFromTeamReturnDTO characterToCharacterFromTeamReturnDTO(Character character) {
        if (character == null)
            return null;
        return new CharacterFromTeamReturnDTO(character.getId(), character.getCharacterName());
    }

    public TeamFromCharacterReturnDTO teamToTeamFromCharacterReturnDTO(Team team) {
        if (team == null)
            return null;
        return new TeamFromCharacterReturnDTO(team.getId(), team.getTeamName(), team.isFinalReady(), team.isActive(),
                team.getGroupPoints(settingsService.getSettings().getMaxGamesCount()), team.getFinalPoints(),
                team.getGames().stream().map(matchPlanFromRegistrationReturnDTOService::gameToGameFromTeamReturnDTO)
                        .collect(Collectors.toSet()));
    }
}
