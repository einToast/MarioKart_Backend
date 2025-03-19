package de.fsr.mariokart_backend.registration.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminRegistrationUpdateService {
    private final TeamRepository teamRepository;
    private final CharacterRepository characterRepository;
    private final RegistrationReturnDTOService registrationReturnDTOService;

    public TeamReturnDTO updateTeam(Long id, TeamInputDTO teamUpdate)
            throws EntityNotFoundException, IllegalArgumentException {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

        if (teamUpdate.getTeamName() != null && !team.getTeamName().equals(teamUpdate.getTeamName())) {
            if (teamRepository.existsByTeamName(teamUpdate.getTeamName())
                    && !teamRepository.findByTeamName(teamUpdate.getTeamName()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Team name already exists");
            }
            team.setTeamName(teamUpdate.getTeamName());
        }

        if (teamUpdate.getCharacterName() != null
                && !team.getCharacter().getCharacterName().equals(teamUpdate.getCharacterName())) {
            var character = characterRepository.findByCharacterName(teamUpdate.getCharacterName())
                    .orElseThrow(() -> new EntityNotFoundException("There is no character with this name."));
            if (character.getTeam() != null && !character.getTeam().getId().equals(id)) {
                throw new IllegalArgumentException("Character is already in a team");
            }
            team.setCharacter(character);
        }

        team.setFinalReady(teamUpdate.isFinalReady());
        team.setActive(teamUpdate.isActive());

        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.save(team));
    }
}