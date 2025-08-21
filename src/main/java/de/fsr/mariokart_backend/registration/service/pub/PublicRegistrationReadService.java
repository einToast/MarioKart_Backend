package de.fsr.mariokart_backend.registration.service.pub;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.repository.GameRepository;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "registration")
public class PublicRegistrationReadService {
    private final TeamRepository teamRepository;
    private final CharacterRepository characterRepository;
    private final GameRepository gameRepository;
    private final RegistrationReturnDTOService registrationReturnDTOService;
    private final AdminRegistrationReadService adminRegistrationReadService;
    private final PublicScheduleReadService publicScheduleReadService;

    @Cacheable(key = "'teams'", sync = true)
    public List<TeamReturnDTO> getTeams() {
        List<TeamReturnDTO> teams = adminRegistrationReadService.getTeams();
        return deleteUnnecessaryInformationFromTeams(teams);
    }

    @Cacheable(key = "'teamsSortedByGroupPoints'", sync = true)
    public List<TeamReturnDTO> getTeamsSortedByGroupPoints() {
        List<TeamReturnDTO> teams = adminRegistrationReadService.getTeamsReturnDTOSortedByGroupPoints();
        return deleteUnnecessaryInformationFromTeams(teams);
    }

    @Cacheable(key = "'availableCharacters'", sync = true)
    public List<CharacterReturnDTO> getAvailableCharacters() {
        return characterRepository.findByTeamIsNull().stream()
                .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                .toList();
    }

    @Cacheable(key = "'teamsNotInRound_' + #roundId", sync = true)
    public List<TeamReturnDTO> getTeamsNotInRound(Long roundId) {
        List<Team> allTeams = teamRepository.findAll();

        List<Team> teamsInRound = getTeamsInRound(roundId);

        List<TeamReturnDTO> teamsNotInRound = allTeams.stream()
                .filter(team -> !teamsInRound.contains(team))
                .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();

        return deleteUnnecessaryInformationFromTeams(teamsNotInRound);
    }

    @Cacheable(key = "'teamsInRound_' + #roundId", sync = true)
    public List<Team> getTeamsInRound(Long roundId) {
        List<Game> gamesInRound = gameRepository.findByRoundId(roundId);

        return gamesInRound.stream()
                .flatMap(game -> game.getPoints().stream()
                        .map(Points::getTeam))
                .distinct()
                .filter(Team::isActive)
                .toList();
    }

    @Cacheable(key = "'teamsInRoundDTO_' + #roundId", sync = true)
    public List<TeamReturnDTO> deleteUnnecessaryInformationFromTeams(List<TeamReturnDTO> teams) {
        if (!publicScheduleReadService.isFinalScheduleCreated()) {
            teams.forEach(team -> team.setGroupPoints(0));
        }
        teams.forEach(team -> team.setFinalPoints(0));
        return teams;
    }

    @Cacheable(key = "'teamsSortedByTeamName'", sync = true)
    public List<TeamReturnDTO> getTeamsSortedByTeamName() {
        List<TeamReturnDTO> teams = teamRepository.findAllByOrderByTeamNameAsc().stream()
                .map(registrationReturnDTOService::teamToTeamReturnDTO)
                .toList();
        return deleteUnnecessaryInformationFromTeams(teams);
    }
}