package de.fsr.mariokart_backend.registration.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.Character;
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

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicRegistrationReadServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RegistrationReturnDTOService registrationReturnDTOService;

    @Mock
    private AdminRegistrationReadService adminRegistrationReadService;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @InjectMocks
    private PublicRegistrationReadService service;

    @Test
    void getTeamsHidesGroupAndFinalPointsWhenFinalScheduleNotCreated() {
        TeamReturnDTO team = new TeamReturnDTO(1L, "Speedsters", new CharacterReturnDTO(1L, "Mario"), false, true, 12, 5, 3);

        when(adminRegistrationReadService.getTeams()).thenReturn(List.of(team));
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);

        List<TeamReturnDTO> result = service.getTeams();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getGroupPoints()).isZero();
        assertThat(result.getFirst().getFinalPoints()).isZero();
        assertThat(result.getFirst().isFinalReady()).isTrue();
    }

    @Test
    void getTeamsSortedByGroupPointsKeepsGroupPointsWhenFinalScheduleCreated() {
        TeamReturnDTO team = new TeamReturnDTO(1L, "Speedsters", new CharacterReturnDTO(1L, "Mario"), false, true, 12, 8, 3);

        when(adminRegistrationReadService.getTeamsReturnDTOSortedByGroupPoints()).thenReturn(List.of(team));
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(true);

        List<TeamReturnDTO> result = service.getTeamsSortedByGroupPoints();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getGroupPoints()).isEqualTo(12);
        assertThat(result.getFirst().getFinalPoints()).isZero();
        assertThat(result.getFirst().isFinalReady()).isTrue();
    }

    @Test
    void getAvailableCharactersMapsCharactersWithoutTeam() {
        Character mario = new Character(1L, "Mario", null);
        CharacterReturnDTO marioDto = new CharacterReturnDTO(1L, "Mario");

        when(characterRepository.findByTeamIsNull()).thenReturn(List.of(mario));
        when(registrationReturnDTOService.characterToCharacterReturnDTO(mario)).thenReturn(marioDto);

        List<CharacterReturnDTO> result = service.getAvailableCharacters();

        assertThat(result).containsExactly(marioDto);
    }

    @Test
    void getTeamsInRoundReturnsDistinctActiveTeamsOnly() {
        Team active = team(1L, "Active", true);
        Team inactive = team(2L, "Inactive", false);

        Game firstGame = new Game();
        firstGame.setPoints(Set.of(points(active), points(inactive)));

        Game secondGame = new Game();
        secondGame.setPoints(Set.of(points(active)));

        when(gameRepository.findByRoundId(9L)).thenReturn(List.of(firstGame, secondGame));

        List<Team> result = service.getTeamsInRound(9L);

        assertThat(result).containsExactly(active);
    }

    @Test
    void getTeamsNotInRoundFiltersRoundTeamsAndSanitizesSensitiveFields() {
        Team inRound = team(1L, "InRound", true);
        Team inactiveInRound = team(2L, "InactiveInRound", false);
        Team free = team(3L, "Free", true);

        Game game = new Game();
        game.setPoints(Set.of(points(inRound), points(inactiveInRound)));

        when(teamRepository.findAll()).thenReturn(List.of(inRound, inactiveInRound, free));
        when(gameRepository.findByRoundId(4L)).thenReturn(List.of(game));

        when(registrationReturnDTOService.teamToTeamReturnDTO(inactiveInRound))
                .thenReturn(new TeamReturnDTO(2L, "InactiveInRound", new CharacterReturnDTO(2L, "Luigi"), false, true, 5, 7, 2));
        when(registrationReturnDTOService.teamToTeamReturnDTO(free))
                .thenReturn(new TeamReturnDTO(3L, "Free", new CharacterReturnDTO(3L, "Peach"), false, true, 6, 9, 1));

        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);

        List<TeamReturnDTO> result = service.getTeamsNotInRound(4L);

        assertThat(result).extracting(TeamReturnDTO::getTeamName).containsExactly("InactiveInRound", "Free");
        assertThat(result).allSatisfy(team -> {
            assertThat(team.getGroupPoints()).isZero();
            assertThat(team.getFinalPoints()).isZero();
            assertThat(team.isFinalReady()).isTrue();
        });
    }

    @Test
    void getTeamsSortedByTeamNameUsesRepositoryOrderAndSanitizes() {
        Team alpha = team(1L, "Alpha", true);
        Team beta = team(2L, "Beta", true);

        when(teamRepository.findAllByOrderByTeamNameAsc()).thenReturn(List.of(alpha, beta));
        when(registrationReturnDTOService.teamToTeamReturnDTO(alpha))
                .thenReturn(new TeamReturnDTO(1L, "Alpha", new CharacterReturnDTO(1L, "Mario"), false, true, 11, 4, 2));
        when(registrationReturnDTOService.teamToTeamReturnDTO(beta))
                .thenReturn(new TeamReturnDTO(2L, "Beta", new CharacterReturnDTO(2L, "Luigi"), false, true, 7, 2, 2));
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(true);

        List<TeamReturnDTO> result = service.getTeamsSortedByTeamName();

        assertThat(result).extracting(TeamReturnDTO::getTeamName).containsExactly("Alpha", "Beta");
        assertThat(result).extracting(TeamReturnDTO::getGroupPoints).containsExactly(11, 7);
        assertThat(result).allSatisfy(team -> {
            assertThat(team.getFinalPoints()).isZero();
            assertThat(team.isFinalReady()).isTrue();
        });
    }

    private static Team team(Long id, String name, boolean active) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        team.setActive(active);
        team.setFinalReady(true);
        return team;
    }

    private static Points points(Team team) {
        Points points = new Points();
        points.setTeam(team);
        return points;
    }
}
