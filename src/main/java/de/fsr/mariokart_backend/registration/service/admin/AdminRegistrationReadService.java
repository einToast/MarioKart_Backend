package de.fsr.mariokart_backend.registration.service.admin;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminRegistrationReadService {
    private final TeamRepository teamRepository;
    private final CharacterRepository characterRepository;
    private final RegistrationReturnDTOService registrationReturnDTOService;
    private final PublicSettingsReadService publicSettingsReadService;

    // TODO: Is this even used?
    public List<TeamReturnDTO> getTeams() {
        return teamRepository.findAll().stream()
                .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();
    }

    public List<TeamReturnDTO> getTeamsSortedByGroupPoints() {
        List<TeamReturnDTO> teams = teamRepository.findAllByOrderByGroupPointsDesc().stream()
                .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();
        if (teams.isEmpty()) {
            teams = getTeams();
        }
        return teams;
    }

    public List<TeamReturnDTO> getTeamsSortedByFinalPoints() {
        List<TeamReturnDTO> teams = teamRepository.findAllByOrderByFinalPointsDescGroupPointsDesc().stream()
                .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();
        if (teams.isEmpty()) {
            teams = getTeams();
        }
        return teams;
    }

    public List<TeamReturnDTO> getFinalTeamsReturnDTO() {
        return getFinalTeams().stream()
                .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();
    }

    public List<Team> getFinalTeams() {
        return teamRepository.findByFinalReadyTrue().stream()
                .sorted(Comparator.comparing(
                        team -> team.getGroupPoints(publicSettingsReadService.getSettings().getMaxGamesCount()),
                        Comparator.reverseOrder()))
                .limit(4)
                .collect(Collectors.toList());
    }

    public List<CharacterReturnDTO> getCharacters() {
        return characterRepository.findAll().stream()
                .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                .toList();
    }
}