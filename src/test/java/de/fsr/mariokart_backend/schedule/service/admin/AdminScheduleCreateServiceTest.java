package de.fsr.mariokart_backend.schedule.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.GameRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleInputDTOService;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminScheduleCreateServiceTest {

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
    private AdminScheduleUpdateService adminScheduleUpdateService;

    @Mock
    private AdminSettingsUpdateService adminSettingsUpdateService;

    @Mock
    private AdminRegistrationReadService adminRegistrationReadService;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @Mock
    private ScheduleInputDTOService scheduleInputDTOService;

    @Mock
    private ScheduleReturnDTOService scheduleReturnDTOService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private WebClient webClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AdminScheduleCreateService service;

    @Test
    void addRoundAssignsRoundNumberWhenZero() {
        Round round = new Round();
        round.setRoundNumber(0);
        round.setStartTime(LocalDateTime.now());
        round.setEndTime(LocalDateTime.now().plusMinutes(20));

        when(scheduleInputDTOService.roundInputDTOToRound(new RoundInputDTO(false))).thenReturn(round);
        when(roundRepository.findAll()).thenReturn(List.of(new Round(), new Round()));
        when(roundRepository.save(round)).thenReturn(round);
        when(scheduleReturnDTOService.roundToRoundDTO(round))
                .thenReturn(new RoundReturnDTO(3L, 3, round.getStartTime(), round.getEndTime(), false, false, null, null));

        RoundReturnDTO dto = service.addRound(new RoundInputDTO(false));

        assertThat(round.getRoundNumber()).isEqualTo(3);
        assertThat(dto.getRoundNumber()).isEqualTo(3);
    }

    @Test
    void addBreakThrowsWhenRoundIsFinalGame() throws EntityNotFoundException {
        Break aBreak = new Break();
        Round finalRound = new Round();
        finalRound.setId(9L);
        finalRound.setFinalGame(true);

        when(scheduleInputDTOService.breakInputDTOToBreak(new BreakInputDTO(9L, 30, false))).thenReturn(aBreak);
        when(breakRepository.save(aBreak)).thenReturn(aBreak);
        when(roundRepository.findById(9L)).thenReturn(Optional.of(finalRound));

        assertThatThrownBy(() -> service.addBreak(new BreakInputDTO(9L, 30, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Final game has no breaks");
    }

    @Test
    void createScheduleThrowsWhenScheduleAlreadyExists() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.createSchedule())
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("already created");
    }

    @Test
    void createScheduleThrowsWhenNotEnoughTeams() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(teamRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.createSchedule())
                .isInstanceOf(NotEnoughTeamsException.class)
                .hasMessageContaining("Not enough teams");
    }

    @Test
    void createFinalScheduleThrowsWhenBaseScheduleMissing() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);

        assertThatThrownBy(() -> service.createFinalSchedule())
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Schedule not created");
    }

    @Test
    void createFinalScheduleThrowsWhenFinalAlreadyExists() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.createFinalSchedule())
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Final schedule already created");
    }

    @Test
    void createFinalScheduleThrowsWhenRoundsUnplayed() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);
        when(publicScheduleReadService.getNumberOfRoundsUnplayed()).thenReturn(2);

        assertThatThrownBy(() -> service.createFinalSchedule())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not all rounds played");
    }

    @Test
    void createFinalScheduleThrowsWhenNotEnoughFinalTeamsReady() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);
        when(publicScheduleReadService.getNumberOfRoundsUnplayed()).thenReturn(0);
        when(teamRepository.findByFinalReadyTrue()).thenReturn(List.of(new Team(), new Team(), new Team()));

        assertThatThrownBy(() -> service.createFinalSchedule())
                .isInstanceOf(NotEnoughTeamsException.class)
                .hasMessageContaining("Not enough teams ready for final");
    }

    @Test
    void createFinalScheduleDoesNotSendMessagesOnValidationFailure() throws NotificationNotSentException {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);

        assertThatThrownBy(() -> service.createFinalSchedule())
                .isInstanceOf(RoundsAlreadyExistsException.class);

        verify(webSocketService, never()).sendMessage("/topic/rounds", "create");
        verify(adminScheduleUpdateService, never()).sendNotificationForNextRound();
    }
}
