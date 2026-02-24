package de.fsr.mariokart_backend.schedule.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundFromBreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ScheduleReturnDTOServiceTest {

    @Mock
    private RegistrationReturnDTOService registrationReturnDTOService;

    @InjectMocks
    private ScheduleReturnDTOService service;

    @Test
    void gameToGameDTOReturnsNullForNullGame() {
        assertThat(service.gameToGameDTO(null)).isNull();
    }

    @Test
    void pointsToPointsDTOReturnsMaxOfGroupAndFinalPoints() {
        Team team = new Team();
        team.setId(1L);
        Points points = new Points();
        points.setId(11L);
        points.setGroupPoints(4);
        points.setFinalPoints(7);
        points.setTeam(team);

        TeamReturnDTO teamDto = new TeamReturnDTO(1L, "A", new CharacterReturnDTO(1L, "Mario"), true, true, 0, 0, 0);
        when(registrationReturnDTOService.teamToTeamReturnDTO(team)).thenReturn(teamDto);

        PointsReturnDTO result = service.pointsToPointsDTO(points);

        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getPoints()).isEqualTo(7);
        assertThat(result.getTeam()).isEqualTo(teamDto);
    }

    @Test
    void roundToRoundDTOMapsGamesAndBreak() {
        Team team = new Team();
        team.setId(1L);
        TeamReturnDTO teamDto = new TeamReturnDTO(1L, "A", new CharacterReturnDTO(1L, "Mario"), true, true, 0, 0, 0);
        when(registrationReturnDTOService.teamToTeamReturnDTO(team)).thenReturn(teamDto);

        Points points = new Points();
        points.setId(1L);
        points.setGroupPoints(3);
        points.setFinalPoints(0);
        points.setTeam(team);

        Game game = new Game();
        game.setId(9L);
        game.setSwitchGame("Blue");
        game.setPoints(Set.of(points));
        points.setGame(game);

        Break aBreak = new Break();
        aBreak.setId(4L);

        Round round = new Round();
        round.setId(5L);
        round.setRoundNumber(2);
        round.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0));
        round.setEndTime(LocalDateTime.of(2026, 1, 1, 10, 20));
        round.setGames(Set.of(game));
        round.setBreakTime(aBreak);

        RoundReturnDTO result = service.roundToRoundDTO(round);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getGames()).hasSize(1);
        GameReturnDTO gameDto = result.getGames().iterator().next();
        assertThat(gameDto.getId()).isEqualTo(9L);
        assertThat(result.getBreakTime()).isEqualTo(aBreak);
    }

    @Test
    void breakToBreakDTOMapsNestedRound() {
        Round round = new Round();
        round.setId(2L);
        round.setRoundNumber(7);

        Break aBreak = new Break();
        aBreak.setId(3L);
        aBreak.setRound(round);

        BreakReturnDTO result = service.breakToBreakDTO(aBreak);

        assertThat(result.getId()).isEqualTo(3L);
        RoundFromBreakReturnDTO nestedRound = result.getRound();
        assertThat(nestedRound.getId()).isEqualTo(2L);
        assertThat(nestedRound.getRoundNumber()).isEqualTo(7);
    }
}
