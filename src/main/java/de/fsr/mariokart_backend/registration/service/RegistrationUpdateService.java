package de.fsr.mariokart_backend.registration.service;

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
public class RegistrationUpdateService {
    private final TeamRepository teamRepository;
    private final CharacterRepository characterRepository;
    private final RegistrationReturnDTOService registrationReturnDTOService;

    public TeamReturnDTO updateTeam(Long id, TeamInputDTO teamCreation)
            throws EntityNotFoundException, IllegalArgumentException {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

        if (teamCreation.getTeamName() != null && !team.getTeamName().equals(teamCreation.getTeamName())) {
            team.setTeamName(teamCreation.getTeamName());
        }

        if (teamCreation.getCharacterName() != null
                && !team.getCharacter().getCharacterName().equals(teamCreation.getCharacterName())) {
            team.setCharacter(characterRepository.findByCharacterName(teamCreation.getCharacterName())
                    .orElseThrow(() -> new EntityNotFoundException("There is no character with this name.")));
        }

        if (teamCreation.isFinalReady() != team.isFinalReady()) {
            team.setFinalReady(teamCreation.isFinalReady());
        }

        if (teamCreation.isActive() != team.isActive()) {
            team.setActive(teamCreation.isActive());
        }

        if (team.getCharacter().getTeam() != null && !team.getCharacter().getTeam().getId().equals(id)) {
            throw new IllegalArgumentException("Character is already in a team");
        }

        if (teamRepository.existsByTeamName(team.getTeamName())
                && !teamRepository.findByTeamName(team.getTeamName()).get().getId().equals(id)) {
            throw new IllegalArgumentException("Team name already exists");
        }

        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.save(team));
    }
} 