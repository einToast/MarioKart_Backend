package de.fsr.mariokart_backend.schedule.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.model.dto.SwitchDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class NextRoundNotificationServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private PointsRepository pointsRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PublicSettingsReadService publicSettingsReadService;

    @Mock
    private AdminNotificationCreateService adminNotificationCreateService;

    @Captor
    private ArgumentCaptor<Map<Long, NotificationRequestDTO>> notificationsCaptor;

    @InjectMocks
    private NextRoundNotificationService service;

    @Test
    void onNextRoundChangedNotifiesTheTeamsOfTheFirstUnplayedRound() {
        Round later = buildRound(8L, 3);
        Round next = buildRound(7L, 2);
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(later, next)));
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, false, 4, null));
        when(pointsRepository.findByGameRoundId(7L)).thenReturn(List.of());
        when(teamRepository.findAll()).thenReturn(List.of());

        service.onNextRoundChanged(new NextRoundChangedEvent(this));

        verify(pointsRepository).findByGameRoundId(7L);
        verify(pointsRepository, never()).findByGameRoundId(8L);
    }

    @Test
    void sendNotificationForNextRoundReturnsWhenNoUnplayedRounds() throws NotificationNotSentException {
        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>());

        service.sendNotificationForNextRound();

        verify(adminNotificationCreateService, never()).sendNotificationToAll(any(), any());
        verify(adminNotificationCreateService, never()).sendNotificationsToTeams(anyMap());
    }

    @Test
    void sendNotificationForNextRoundSendsBreakNotificationWhenBreakIsActive() throws NotificationNotSentException {
        Round round = buildRound(5L, 1);
        Break aBreak = new Break();
        aBreak.setBreakEnded(false);
        round.setBreakTime(aBreak);

        when(roundRepository.findByPlayedFalse()).thenReturn(new ArrayList<>(List.of(round)));

        service.sendNotificationForNextRound();

        verify(adminNotificationCreateService).sendNotificationToAll(
                "It's pizza time! 🍕",
                "Pizzapause!");
        verify(adminNotificationCreateService, never()).sendNotificationsToTeams(anyMap());
    }

    @Test
    void sendNotificationForNextRoundNamesTheSwitchAndFallsBackToItsNumber() throws NotificationNotSentException {
        Round round = buildRound(7L, 1);

        Team team1 = buildTeam(1L);
        Team team2 = buildTeam(2L);
        Team team3 = buildTeam(3L);

        Game game1 = buildGame(100L, 0);
        Game game2 = buildGame(101L, 1);

        TournamentDTO settings = new TournamentDTO(true, false, 4, null);
        settings.setSwitches(List.of(new SwitchDTO("Blau", "#9DAEDA")));
        when(publicSettingsReadService.getSettings()).thenReturn(settings);
        when(pointsRepository.findByGameRoundId(7L)).thenReturn(List.of(
                buildPoints(team1, game1), buildPoints(team2, game1), buildPoints(team3, game2)));
        when(teamRepository.findAll()).thenReturn(List.of(team1, team2, team3, buildTeam(4L)));

        service.sendNotificationForNextRound(round);

        verify(adminNotificationCreateService).sendNotificationsToTeams(notificationsCaptor.capture());
        assertThat(notificationsCaptor.getValue()).containsOnly(
                Map.entry(1L, new NotificationRequestDTO("Du spielst jetzt an Switch Blau!",
                        "Du spielst jetzt an Switch Blau! Streng dich an!")),
                Map.entry(2L, new NotificationRequestDTO("Du spielst jetzt an Switch Blau!",
                        "Du spielst jetzt an Switch Blau! Streng dich an!")),
                Map.entry(3L, new NotificationRequestDTO("Du spielst jetzt an Switch 2!",
                        "Du spielst jetzt an Switch 2! Streng dich an!")),
                Map.entry(4L, new NotificationRequestDTO("Du spielst jetzt nicht!", "Gönn dir eine Pause!")));
    }

    @Test
    void sendNotificationForNextRoundKeepsGoingWhenNoNotificationArrives() throws NotificationNotSentException {
        Round round = buildRound(7L, 1);
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, false, 4, null));
        when(pointsRepository.findByGameRoundId(7L)).thenReturn(List.of());
        when(teamRepository.findAll()).thenReturn(List.of(buildTeam(1L)));
        doThrow(new NotificationNotSentException("push service down"))
                .when(adminNotificationCreateService).sendNotificationsToTeams(anyMap());

        service.sendNotificationForNextRound(round);

        verify(adminNotificationCreateService).sendNotificationsToTeams(anyMap());
    }

    private Round buildRound(Long id, int number) {
        Round round = new Round();
        round.setId(id);
        round.setRoundNumber(number);
        round.setStartTime(LocalDateTime.now().plusMinutes(number));
        round.setEndTime(round.getStartTime().plusMinutes(20));
        return round;
    }

    private Team buildTeam(Long id) {
        Team team = new Team();
        team.setId(id);
        return team;
    }

    private Game buildGame(Long id, int switchIndex) {
        Game game = new Game();
        game.setId(id);
        game.setSwitchIndex(switchIndex);
        return game;
    }

    private Points buildPoints(Team team, Game game) {
        Points points = new Points();
        points.setTeam(team);
        points.setGame(game);
        return points;
    }
}
