package de.fsr.mariokart_backend.registration.service.pub;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationInputDTOService;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicRegistrationCreateService {
    private final TeamRepository teamRepository;
    private final RoundRepository roundRepository;
    private final RegistrationInputDTOService registrationInputDTOService;
    private final RegistrationReturnDTOService registrationReturnDTOService;
    private final PublicSettingsReadService settingsReadService;

    public TeamReturnDTO registerTeam(TeamInputDTO teamCreation)
            throws IllegalArgumentException, EntityNotFoundException, RoundsAlreadyExistsException {
        if (!settingsReadService.getSettings().getRegistrationOpen()) {
            throw new IllegalStateException("Registration is closed");
        }
        if (!settingsReadService.getSettings().getTournamentOpen()) {
            throw new IllegalStateException("Tournament is closed");
        }

        if (!roundRepository.findAll().isEmpty()) {
            throw new RoundsAlreadyExistsException("Match plan already exists");
        }

        Team team = registrationInputDTOService.teamInputDTOToTeam(teamCreation);
        team.setFinalReady(true);
        team.setActive(true);

        if (team.getCharacter().getTeam() != null) {
            throw new IllegalArgumentException("Character is already in a team");
        }
        if (teamRepository.existsByTeamName(team.getTeamName())) {
            throw new IllegalArgumentException("Team name already exists");
        }

        team.getCharacter().setTeam(team);
        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.save(team));
    }
}