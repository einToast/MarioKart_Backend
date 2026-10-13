package de.fsr.mariokart_backend.registration.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RegistrationReturnDTOService {

    private final PublicSettingsReadService publicSettingsReadService;
    private final PointsRepository pointsRepository;

    public CharacterReturnDTO characterToCharacterReturnDTO(Character character) {
        if (character == null)
            return null;
        return new CharacterReturnDTO(character.getId(), character.getCharacterName());
    }

    public TeamReturnDTO teamToTeamReturnDTO(Team team) {
        if (team == null)
            return null;
        int maxGames = publicSettingsReadService.getSettings().getMaxGamesCount();
        double groupPoints = team.getWeightedGroupPoints(maxGames, pointsRepository.getSmallestGroupGameTeamCount());
        return new TeamReturnDTO(team.getId(), team.getTeamName(),
                characterToCharacterReturnDTO(team.getCharacter()), team.isFinalReady(), team.isActive(),
                (int) Math.round(groupPoints), team.getFinalPoints(),
                team.getNumberOfGamesPlayed(maxGames));
    }

}
