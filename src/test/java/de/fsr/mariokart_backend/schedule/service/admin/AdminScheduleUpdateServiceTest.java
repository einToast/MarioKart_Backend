package de.fsr.mariokart_backend.schedule.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputFullDTO;
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
    void updateRoundPlayedDoesNotSendWebSocketWhenPlayedStateUnchanged()
            throws EntityNotFoundException, RoundsAlreadyExistsException, NotificationNotSentException {
        Round round = buildRound(2L, 1, false, false);

        when(roundRepository.findById(2L)).thenReturn(Optional.of(round));
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(round)));
        when(adminScheduleReadService.isBreakFinished()).thenReturn(true);
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class)))
                .thenReturn(new RoundReturnDTO(2L, 1, LocalDateTime.now(), LocalDateTime.now(),
                        false, false, null, null));

        service.updateRoundPlayed(2L, new RoundInputDTO(false));

        verify(webSocketService, never()).sendMessage("/topic/rounds", "update");
    }

    @Test
    void updateRoundPlayedRecalculatesBreakAndFollowingRoundsWhenBreakNotFinished()
            throws EntityNotFoundException, RoundsAlreadyExistsException, NotificationNotSentException {
        Round currentRound = buildRound(1L, 1, false, false);
        Round breakRound = buildRound(2L, 2, false, false);
        Break aBreak = new Break();
        aBreak.setId(50L);
        aBreak.setStartTime(LocalDateTime.now().plusMinutes(10));
        aBreak.setEndTime(LocalDateTime.now().plusMinutes(40));
        aBreak.setBreakEnded(false);
        aBreak.setRound(breakRound);
        breakRound.setBreakTime(aBreak);
        Round afterBreakRound = buildRound(3L, 3, false, false);

        when(roundRepository.findById(1L)).thenReturn(Optional.of(currentRound));
        when(roundRepository.findByPlayedFalse())
                .thenReturn(new ArrayList<>(List.of(currentRound, breakRound, afterBreakRound)),
                        new ArrayList<>(List.of(breakRound, afterBreakRound)));
        when(adminScheduleReadService.isBreakFinished()).thenReturn(false);
        when(roundRepository.findAll()).thenReturn(new ArrayList<>(List.of(currentRound, breakRound, afterBreakRound)));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRepository.save(any(Break.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, LocalDateTime.now(), LocalDateTime.now(), false, true, null, null));

        service.updateRoundPlayed(1L, new RoundInputDTO(true));

        verify(breakRepository, atLeastOnce()).save(any(Break.class));
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
    void updatePointsThrowsWhenPointsEntryDoesNotExist() {
        when(pointsRepository.findByGameIdAndTeamId(99L, 88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePoints(77L, 99L, 88L, new PointsInputDTO(5)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("points");
    }

    @Test
    void updateBreakThrowsWhenScheduleIsMissing() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);

        assertThatThrownBy(() -> service.updateBreak(new BreakInputDTO(1L, 30, false)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Schedule not created");
    }

    @Test
    void updateBreakMovesBreakAndSendsNotificationWhenStatusChanges()
            throws EntityNotFoundException, NotificationNotSentException {
        Round oldRound = buildRound(1L, 1, false, false);
        Round newRound = buildRound(2L, 2, false, false);
        Round followingRound = buildRound(3L, 3, false, false);

        Break aBreak = new Break();
        aBreak.setId(10L);
        aBreak.setStartTime(LocalDateTime.now().plusMinutes(10));
        aBreak.setEndTime(LocalDateTime.now().plusMinutes(40));
        aBreak.setBreakEnded(false);
        aBreak.setRound(oldRound);
        oldRound.setBreakTime(aBreak);

        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);
        when(breakRepository.findAll()).thenReturn(List.of(aBreak));
        when(roundRepository.findById(2L)).thenReturn(Optional.of(newRound));
        when(roundRepository.findAll()).thenReturn(new ArrayList<>(List.of(oldRound, newRound, followingRound)));
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(newRound, followingRound)));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRepository.save(any(Break.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Game game = new Game();
        game.setId(70L);
        game.setSwitchGame("Blau");
        Team team = buildTeam(9L, "Team Nine", "Yoshi");
        Points points = buildPoints(team, game, 0, 0);
        when(gameRepository.findByRoundId(2L)).thenReturn(List.of(game));
        when(pointsRepository.findByGameId(70L)).thenReturn(List.of(points));
        when(teamRepository.findAll()).thenReturn(List.of(team));
        when(scheduleReturnDTOService.breakToBreakDTO(any(Break.class)))
                .thenReturn(new BreakReturnDTO(11L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(45), true, null));

        BreakReturnDTO dto = service.updateBreak(new BreakInputDTO(2L, 45, true));

        assertThat(dto.isBreakEnded()).isTrue();
        assertThat(oldRound.getBreakTime()).isNull();
        assertThat(newRound.getBreakTime()).isNotNull();
        verify(breakRepository).delete(aBreak);
        verify(webSocketService).sendMessage("/topic/rounds", "update");
        verify(adminNotificationCreateService, atLeastOnce()).sendNotificationToTeam(anyLong(), any(), any());
    }

    @Test
    void updateBreakUpdatesFollowingRoundsWithoutNotificationWhenBreakStillActive()
            throws EntityNotFoundException, NotificationNotSentException {
        Round previousRound = buildRound(1L, 1, false, true);
        Round breakRound = buildRound(2L, 2, false, false);
        Round afterBreakRound = buildRound(3L, 3, false, false);

        Break aBreak = new Break();
        aBreak.setId(12L);
        aBreak.setStartTime(LocalDateTime.now().plusMinutes(20));
        aBreak.setEndTime(LocalDateTime.now().plusMinutes(50));
        aBreak.setBreakEnded(false);
        aBreak.setRound(breakRound);
        breakRound.setBreakTime(aBreak);

        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);
        when(breakRepository.findAll()).thenReturn(List.of(aBreak));
        when(roundRepository.findById(2L)).thenReturn(Optional.of(breakRound));
        when(roundRepository.findAll()).thenReturn(new ArrayList<>(List.of(previousRound, breakRound, afterBreakRound)));
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(breakRound, afterBreakRound)));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRepository.save(any(Break.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.breakToBreakDTO(any(Break.class)))
                .thenReturn(new BreakReturnDTO(12L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), false, null));

        service.updateBreak(new BreakInputDTO(2L, 30, null));

        verify(webSocketService).sendMessage("/topic/rounds", "update");
        verify(adminNotificationCreateService, never()).sendNotificationToAll(any(), any());
        verify(breakRepository, never()).delete(any(Break.class));
    }

    @Test
    void updateRoundUpdatesGroupPointsByCharacterName()
            throws EntityNotFoundException, RoundsAlreadyExistsException, NotificationNotSentException {
        Team team1 = buildTeam(1L, "Team One", "Mario");
        Team team2 = buildTeam(2L, "Team Two", "Luigi");

        Round round = buildRound(1L, 1, false, false);
        Game game = new Game();
        game.setId(11L);
        game.setRound(round);

        Points points1 = buildPoints(team1, game, 1, 5);
        Points points2 = buildPoints(team2, game, 2, 6);
        game.setPoints(Set.of(points1, points2));
        round.setGames(Set.of(game));

        PointsInputFullDTO in1 = new PointsInputFullDTO(10, new TeamInputDTO("Team One", "Mario"));
        PointsInputFullDTO in2 = new PointsInputFullDTO(7, new TeamInputDTO("Team Two", "Luigi"));
        RoundInputFullDTO input = new RoundInputFullDTO(false, new GameInputFullDTO[] {
                new GameInputFullDTO(11L, new PointsInputFullDTO[] {in1, in2})
        });

        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(round)));
        when(adminScheduleReadService.isBreakFinished()).thenReturn(true);
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pointsRepository.save(any(Points.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, LocalDateTime.now(), LocalDateTime.now(), false, false, null, null));

        service.updateRound(1L, input);

        assertThat(points1.getGroupPoints()).isEqualTo(10);
        assertThat(points2.getGroupPoints()).isEqualTo(7);
        assertThat(points1.getFinalPoints()).isEqualTo(5);
        verify(pointsRepository, times(2)).save(any(Points.class));
    }

    @Test
    void updateRoundUpdatesFinalPointsByCharacterName()
            throws EntityNotFoundException, RoundsAlreadyExistsException, NotificationNotSentException {
        Team team1 = buildTeam(1L, "Team One", "Mario");
        Team team2 = buildTeam(2L, "Team Two", "Luigi");

        Round round = buildRound(1L, 1, true, false);
        Game game = new Game();
        game.setId(11L);
        game.setRound(round);

        Points points1 = buildPoints(team1, game, 3, 1);
        Points points2 = buildPoints(team2, game, 4, 2);
        game.setPoints(Set.of(points1, points2));
        round.setGames(Set.of(game));

        PointsInputFullDTO in1 = new PointsInputFullDTO(14, new TeamInputDTO("Team One", "Mario"));
        PointsInputFullDTO in2 = new PointsInputFullDTO(9, new TeamInputDTO("Team Two", "Luigi"));
        RoundInputFullDTO input = new RoundInputFullDTO(false, new GameInputFullDTO[] {
                new GameInputFullDTO(11L, new PointsInputFullDTO[] {in1, in2})
        });

        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(round)));
        when(adminScheduleReadService.isBreakFinished()).thenReturn(true);
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pointsRepository.save(any(Points.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class)))
                .thenReturn(new RoundReturnDTO(1L, 1, LocalDateTime.now(), LocalDateTime.now(), true, false, null, null));

        service.updateRound(1L, input);

        assertThat(points1.getFinalPoints()).isEqualTo(14);
        assertThat(points2.getFinalPoints()).isEqualTo(9);
        assertThat(points1.getGroupPoints()).isEqualTo(3);
        verify(pointsRepository, times(2)).save(any(Points.class));
    }

    @Test
    void updateGameUpdatesGroupPointsForRegularRound() throws EntityNotFoundException {
        Team team = buildTeam(8L, "Team Eight", "Peach");
        Round round = buildRound(30L, 1, false, false);
        Game game = new Game();
        game.setId(40L);
        game.setRound(round);

        Points point = buildPoints(team, game, 0, 5);
        game.setPoints(Set.of(point));

        GameInputFullDTO input = new GameInputFullDTO(40L,
                new PointsInputFullDTO[] {new PointsInputFullDTO(12, new TeamInputDTO("Team Eight", "Peach"))});

        when(gameRepository.findById(40L)).thenReturn(Optional.of(game));
        when(pointsRepository.save(any(Points.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(game)).thenReturn(game);
        when(scheduleReturnDTOService.gameToGameDTO(game)).thenReturn(new GameReturnDTO(40L, "Blau", null, null));

        GameReturnDTO dto = service.updateGame(40L, input);

        assertThat(point.getGroupPoints()).isEqualTo(12);
        assertThat(point.getFinalPoints()).isEqualTo(5);
        assertThat(dto.getId()).isEqualTo(40L);
    }

    @Test
    void updateGameUpdatesFinalPointsForFinalRound() throws EntityNotFoundException {
        Team team = buildTeam(9L, "Team Nine", "Yoshi");
        Round round = buildRound(31L, 2, true, false);
        Game game = new Game();
        game.setId(41L);
        game.setRound(round);

        Points point = buildPoints(team, game, 2, 0);
        game.setPoints(Set.of(point));

        GameInputFullDTO input = new GameInputFullDTO(41L,
                new PointsInputFullDTO[] {new PointsInputFullDTO(15, new TeamInputDTO("Team Nine", "Yoshi"))});

        when(gameRepository.findById(41L)).thenReturn(Optional.of(game));
        when(pointsRepository.save(any(Points.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(game)).thenReturn(game);
        when(scheduleReturnDTOService.gameToGameDTO(game)).thenReturn(new GameReturnDTO(41L, "Rot", null, null));

        service.updateGame(41L, input);

        assertThat(point.getFinalPoints()).isEqualTo(15);
        assertThat(point.getGroupPoints()).isEqualTo(2);
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

    @Test
    void sendNotificationForNextRoundSendsPlayingAndIdleTeamNotifications() throws NotificationNotSentException {
        Round round = buildRound(7L, 1, false, false);

        Team team1 = buildTeam(1L, "Team One", "Mario");
        Team team2 = buildTeam(2L, "Team Two", "Luigi");
        Team team3 = buildTeam(3L, "Team Three", "Peach");
        Team team4 = buildTeam(4L, "Team Four", "Yoshi");

        Game game1 = new Game();
        game1.setId(100L);
        game1.setSwitchGame("Blau");

        Game game2 = new Game();
        game2.setId(101L);
        game2.setSwitchGame("Rot");

        when(gameRepository.findByRoundId(7L)).thenReturn(List.of(game1, game2));
        when(pointsRepository.findByGameId(100L))
                .thenReturn(List.of(buildPoints(team1, game1, 0, 0), buildPoints(team2, game1, 0, 0)));
        when(pointsRepository.findByGameId(101L))
                .thenReturn(List.of(buildPoints(team3, game2, 0, 0)));
        when(teamRepository.findAll()).thenReturn(List.of(team1, team2, team3, team4));

        service.sendNotificationForNextRound(round);

        verify(adminNotificationCreateService).sendNotificationToTeam(
                team1.getId(),
                "Du spielst jetzt an Switch Blau!",
                "Du spielst jetzt an Switch Blau! Streng dich an!");
        verify(adminNotificationCreateService).sendNotificationToTeam(
                team2.getId(),
                "Du spielst jetzt an Switch Blau!",
                "Du spielst jetzt an Switch Blau! Streng dich an!");
        verify(adminNotificationCreateService).sendNotificationToTeam(
                team3.getId(),
                "Du spielst jetzt an Switch Rot!",
                "Du spielst jetzt an Switch Rot! Streng dich an!");
        verify(adminNotificationCreateService).sendNotificationToTeam(
                team4.getId(),
                "Du spielst jetzt nicht!",
                "Gönn dir eine Pause!");
    }

    private Round buildRound(Long id, int number, boolean finalGame, boolean played) {
        Round round = new Round();
        round.setId(id);
        round.setRoundNumber(number);
        round.setFinalGame(finalGame);
        round.setPlayed(played);
        round.setStartTime(LocalDateTime.now().plusMinutes(number));
        round.setEndTime(round.getStartTime().plusMinutes(20));
        return round;
    }

    private Team buildTeam(Long id, String name, String characterName) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        Character character = new Character();
        character.setCharacterName(characterName);
        team.setCharacter(character);
        return team;
    }

    private Points buildPoints(Team team, Game game, int groupPoints, int finalPoints) {
        Points points = new Points();
        points.setTeam(team);
        points.setGame(game);
        points.setGroupPoints(groupPoints);
        points.setFinalPoints(finalPoints);
        return points;
    }
}
