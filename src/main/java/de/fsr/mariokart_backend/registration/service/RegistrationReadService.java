package de.fsr.mariokart_backend.registration.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.repository.GameRepository;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.settings.service.SettingsReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RegistrationReadService {
    private final TeamRepository teamRepository;
    private final CharacterRepository characterRepository;
    private final RegistrationReturnDTOService registrationReturnDTOService;
    private final SettingsReadService settingsReadService;
    private final GameRepository gameRepository;

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

    public TeamReturnDTO getTeamById(Long id) throws EntityNotFoundException {
        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this ID.")));
    }

    public TeamReturnDTO getTeamByName(String teamName) throws EntityNotFoundException {
        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.findByTeamName(teamName)
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this name.")));
    }

    public List<Team> getFinalTeams() {
        return teamRepository.findByFinalReadyTrue().stream()
                .sorted(Comparator.comparing(
                        team -> team.getGroupPoints(settingsReadService.getSettings().getMaxGamesCount()),
                        Comparator.reverseOrder()))
                .limit(4)
                .collect(Collectors.toList());
    }

    public List<CharacterReturnDTO> getCharacters() {
        return characterRepository.findAll().stream()
                .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                .toList();
    }

    public List<CharacterReturnDTO> getAvailableCharacters() {
        return characterRepository.findByTeamIsNull().stream()
                .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                .toList();
    }

    public List<CharacterReturnDTO> getTakenCharacters() {
        return characterRepository.findByTeamIsNotNull().stream()
                .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                .toList();
    }

    public CharacterReturnDTO getCharacterById(Long id) throws EntityNotFoundException {
        return registrationReturnDTOService.characterToCharacterReturnDTO(characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no character with this ID.")));
    }

    public CharacterReturnDTO getCharacterByName(String characterName) throws EntityNotFoundException {
        return registrationReturnDTOService
                .characterToCharacterReturnDTO(characterRepository.findByCharacterName(characterName)
                        .orElseThrow(() -> new EntityNotFoundException("There is no character with this name.")));
    }

    public List<TeamReturnDTO> getTeamsNotInRound(Long roundId) {
        // Hole alle Teams
        List<Team> allTeams = teamRepository.findAll();

        List<Team> teamsInRound = getTeamsInRound(roundId);

        // vergleiche alle teams in round mit allen teams
        // wenn ein team nicht in round ist, füge es zu teamsNotInRound hinzu
        return allTeams.stream()
                .filter(team -> !teamsInRound.contains(team))
                .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();
    }

    public List<Team> getTeamsInRound(Long roundId) {
        // Hole alle Spiele der Runde
        List<Game> gamesInRound = gameRepository.findByRoundId(roundId);

        // Sammle alle Teams, die in den Spielen der Runde vorkommen
        return gamesInRound.stream()
                .flatMap(game -> game.getPoints().stream()
                        .map(Points::getTeam))
                .distinct()
                .filter(Team::isActive)
                // .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();
    }
}