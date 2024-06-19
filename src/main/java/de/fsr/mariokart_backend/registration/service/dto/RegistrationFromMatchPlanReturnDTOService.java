package de.fsr.mariokart_backend.registration.service.dto;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamFromGameReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamFromPointsReturnDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationFromMatchPlanReturnDTOService {

    private final RegistrationReturnDTOService registrationReturnDTOService;

    public TeamFromGameReturnDTO teamToTeamFromGameReturnDTO(Team team){
        if (team == null)
            return null;
        return new TeamFromGameReturnDTO(team.getId(), team.getTeamName(), registrationReturnDTOService.characterToCharacterFromTeamReturnDTO(team.getCharacter()),team.isFinalReady(), team.getGroupPoints(), team.getFinalPoints());
    }

    public TeamFromPointsReturnDTO teamToTeamFromPointsReturnDTO(Team team){
        if (team == null)
            return null;
        return new TeamFromPointsReturnDTO(team.getId(), team.getTeamName(), registrationReturnDTOService.characterToCharacterFromTeamReturnDTO(team.getCharacter()),team.isFinalReady(), team.getGroupPoints(), team.getFinalPoints());
    }
}
