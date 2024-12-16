package de.fsr.mariokart_backend.registration.service.dto;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationInputDTOService {

    private final CharacterRepository characterRepository;

    public Team teamInputDTOToTeam(TeamInputDTO teamInputDTO) throws EntityNotFoundException {
        Team team = new Team();
        team.setTeamName(teamInputDTO.getTeamName());
        team.setCharacter(characterRepository.findByCharacterName(teamInputDTO.getCharacterName()).orElseThrow(() -> new EntityNotFoundException("There is no character with this name.")));
        team.setFinalReady(teamInputDTO.isFinalReady());
        team.setActive(teamInputDTO.isActive());
        return team;
    }


}
