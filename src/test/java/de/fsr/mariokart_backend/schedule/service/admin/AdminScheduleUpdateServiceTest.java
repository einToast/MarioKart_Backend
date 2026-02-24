package de.fsr.mariokart_backend.schedule.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.GameRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminScheduleUpdateServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PointsRepository pointsRepository;

    @Mock
    private BreakRepository breakRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AdminScheduleReadService adminScheduleReadService;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @Mock
    private ScheduleReturnDTOService scheduleReturnDTOService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private AdminNotificationCreateService adminNotificationCreateService;

    @InjectMocks
    private AdminScheduleUpdateService service;

    @Test
    void updateRoundPlayedThrowsWhenRoundMissing() {
        when(roundRepository.findById(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRoundPlayed(11L, new RoundInputDTO(true)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("round");
    }

    @Test
    void updateRoundPlayedThrowsWhenBreakNotFinishedAndNoBreakRound() {
        Round round = buildRound(1L, 1, false, false);
        List<Round> unplayedRounds = new ArrayList<>(List.of(round));

        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.findByPlayedFalse()).thenReturn(unplayedRounds);
        when(adminScheduleReadService.isBreakFinished()).thenReturn(false);

        assertThatThrownBy(() -> service.updateRoundPlayed(1L, new RoundInputDTO(true)))
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Break not finished");
    }

    @Test
    void updateRoundPlayedSendsWebSocketWhenPlayedStateChanges()
            throws EntityNotFoundException, RoundsAlreadyExistsException, NotificationNotSentException {
        Round round = buildRound(1L, 1, false, false);
        List<Round> firstUnplayed = new ArrayList<>(List.of(round));

        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.findByPlayedFalse()).thenReturn(firstUnplayed, new ArrayList<>());
        when(adminScheduleReadService.isBreakFinished()).thenReturn(true);
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, LocalDateTime.now(), LocalDateTime.now(),
                        false, true, null, null));

        RoundReturnDTO result = service.updateRoundPlayed(1L, new RoundInputDTO(true));

        assertThat(result.isPlayed()).isTrue();
        verify(webSocketService).sendMessage("/topic/rounds", "update");
    }

    @Test
    void updatePointsUpdatesGroupPointsForRegularRound() throws EntityNotFoundException {
        Team team = new Team();
        team.setId(21L);
        Game game = new Game();
        game.setId(31L);
        Round round = buildRound(41L, 2, false, false);
        game.setRound(round);

        Points points = new Points();
        points.setId(51L);
        points.setGroupPoints(0);
        points.setFinalPoints(9);
        points.setGame(game);
        points.setTeam(team);

        when(pointsRepository.findByGameIdAndTeamId(31L, 21L)).thenReturn(Optional.of(points));
        when(roundRepository.findById(41L)).thenReturn(Optional.of(round));
        when(gameRepository.findById(31L)).thenReturn(Optional.of(game));
        when(teamRepository.findById(21L)).thenReturn(Optional.of(team));
        when(pointsRepository.save(points)).thenReturn(points);
        when(scheduleReturnDTOService.pointsToPointsDTO(points))
                .thenReturn(new PointsReturnDTO(51L, 7, null));

        PointsReturnDTO dto = service.updatePoints(41L, 31L, 21L, new PointsInputDTO(7));

        assertThat(points.getGroupPoints()).isEqualTo(7);
        assertThat(points.getFinalPoints()).isEqualTo(9);
        assertThat(dto.getPoints()).isEqualTo(7);
    }

    @Test
    void updatePointsUpdatesFinalPointsForFinalRound() throws EntityNotFoundException {
        Team team = new Team();
        team.setId(22L);
        Game game = new Game();
        game.setId(32L);
        Round round = buildRound(42L, 3, true, false);
        game.setRound(round);

        Points points = new Points();
        points.setId(52L);
        points.setGroupPoints(4);
        points.setFinalPoints(0);
        points.setGame(game);
        points.setTeam(team);

        when(pointsRepository.findByGameIdAndTeamId(32L, 22L)).thenReturn(Optional.of(points));
        when(roundRepository.findById(42L)).thenReturn(Optional.of(round));
        when(gameRepository.findById(32L)).thenReturn(Optional.of(game));
        when(teamRepository.findById(22L)).thenReturn(Optional.of(team));
        when(pointsRepository.save(points)).thenReturn(points);
        when(scheduleReturnDTOService.pointsToPointsDTO(points))
                .thenReturn(new PointsReturnDTO(52L, 11, null));

        PointsReturnDTO dto = service.updatePoints(42L, 32L, 22L, new PointsInputDTO(11));

        assertThat(points.getFinalPoints()).isEqualTo(11);
        assertThat(points.getGroupPoints()).isEqualTo(4);
        assertThat(dto.getPoints()).isEqualTo(11);
    }

    @Test
    void updateBreakThrowsWhenScheduleIsMissing() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);

        assertThatThrownBy(() -> service.updateBreak(new BreakInputDTO(1L, 30, false)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Schedule not created");
    }

    @Test
    void sendNotificationForNextRoundReturnsWhenNoUnplayedRounds() throws NotificationNotSentException {
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>());

        service.sendNotificationForNextRound();

        verify(adminNotificationCreateService, never()).sendNotificationToAll(any(), any());
        verify(adminNotificationCreateService, never()).sendNotificationToTeam(any(), any(), any());
    }

    @Test
    void sendNotificationForNextRoundSendsBreakNotificationWhenBreakIsActive() throws NotificationNotSentException {
        Round round = buildRound(5L, 1, false, false);
        Break aBreak = new Break();
        aBreak.setBreakEnded(false);
        round.setBreakTime(aBreak);

        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(round)));
        when(gameRepository.findByRoundId(5L)).thenReturn(List.of());

        service.sendNotificationForNextRound();

        verify(adminNotificationCreateService).sendNotificationToAll(
                eq("It's pizza time! 🍕"),
                eq("Pizzapause!"));
        verify(adminNotificationCreateService, never()).sendNotificationToTeam(any(), any(), any());
    }

    private Round buildRound(Long id, int number, boolean finalGame, boolean played) {
        Round round = new Round();
        round.setId(id);
        round.setRoundNumber(number);
        round.setFinalGame(finalGame);
        round.setPlayed(played);
        round.setStartTime(LocalDateTime.now());
        round.setEndTime(LocalDateTime.now().plusMinutes(20));
        return round;
    }
}
