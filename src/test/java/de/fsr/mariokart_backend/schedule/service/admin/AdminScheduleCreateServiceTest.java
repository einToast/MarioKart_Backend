package de.fsr.mariokart_backend.schedule.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
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
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.ScheduleDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.GameRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleInputDTOService;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
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

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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
    void addRoundKeepsProvidedRoundNumber() {
        Round round = new Round();
        round.setRoundNumber(7);
        round.setStartTime(LocalDateTime.now());
        round.setEndTime(LocalDateTime.now().plusMinutes(20));

        when(scheduleInputDTOService.roundInputDTOToRound(new RoundInputDTO(false))).thenReturn(round);
        when(roundRepository.save(round)).thenReturn(round);
        when(scheduleReturnDTOService.roundToRoundDTO(round))
                .thenReturn(new RoundReturnDTO(7L, 7, round.getStartTime(), round.getEndTime(), false, false, null, null));

        RoundReturnDTO dto = service.addRound(new RoundInputDTO(false));

        assertThat(dto.getRoundNumber()).isEqualTo(7);
        verify(roundRepository, never()).findAll();
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
    void addBreakAssignsBreakToRoundOnSuccess() throws EntityNotFoundException {
        Break aBreak = new Break();
        aBreak.setId(1L);
        aBreak.setBreakEnded(false);
        Round round = buildRound(9L, 3, false, false);
        BreakInputDTO input = new BreakInputDTO(9L, 30, false);

        when(scheduleInputDTOService.breakInputDTOToBreak(input)).thenReturn(aBreak);
        when(breakRepository.save(aBreak)).thenReturn(aBreak);
        when(roundRepository.findById(9L)).thenReturn(Optional.of(round));
        when(roundRepository.save(round)).thenReturn(round);
        when(scheduleReturnDTOService.breakToBreakDTO(aBreak))
                .thenReturn(new BreakReturnDTO(1L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), false, null));

        BreakReturnDTO dto = service.addBreak(input);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(round.getBreakTime()).isSameAs(aBreak);
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
    void createScheduleThrowsWhenScheduleGeneratorRequestFails() {
        List<Team> teams = buildTeams(16);
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(teamRepository.findAll()).thenReturn(teams);
        when(webClient.post()).thenThrow(new RuntimeException("generator down"));

        assertThatThrownBy(() -> service.createSchedule())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send request");
    }

    @Test
    void createScheduleThrowsWhenScheduleGeneratorResponseCannotBeParsed() {
        List<Team> teams = buildTeams(16);
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(teamRepository.findAll()).thenReturn(teams);
        when(webClient.post().uri("/schedule").bodyValue(anyMap()).retrieve().bodyToMono(String.class))
                .thenReturn(Mono.just("invalid-json"));
        when(objectMapper.readValue("invalid-json", ScheduleDTO.class)).thenThrow(new RuntimeException("bad json"));

        assertThatThrownBy(() -> service.createSchedule())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse JSON response");
    }

    @Test
    void createScheduleCreatesRoundsAndBreakAndSendsSideEffects()
            throws Exception {
        List<Team> teams = buildTeams(16);
        List<Round> savedRounds = new ArrayList<>();
        AtomicLong roundIds = new AtomicLong(1L);
        AtomicLong gameIds = new AtomicLong(100L);

        ScheduleDTO scheduleDTO = new ScheduleDTO(12, buildSchedulePlan(6, 1));

        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false, false);
        when(teamRepository.findAll()).thenReturn(teams);
        when(webClient.post().uri("/schedule").bodyValue(anyMap()).retrieve().bodyToMono(String.class))
                .thenReturn(Mono.just("payload"));
        when(objectMapper.readValue("payload", ScheduleDTO.class)).thenReturn(scheduleDTO);
        when(scheduleInputDTOService.breakInputDTOToBreak(any(BreakInputDTO.class))).thenAnswer(invocation -> {
            Break createdBreak = new Break();
            BreakInputDTO input = invocation.getArgument(0);
            createdBreak.setBreakEnded(Boolean.TRUE.equals(input.getBreakEnded()));
            return createdBreak;
        });

        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            if (round.getId() == null) {
                round.setId(roundIds.getAndIncrement());
            }
            savedRounds.removeIf(existing -> existing.getId().equals(round.getId()));
            savedRounds.add(round);
            return round;
        });
        when(roundRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(savedRounds));
        when(roundRepository.findById(anyLong())).thenAnswer(invocation -> savedRounds.stream()
                .filter(round -> round.getId().equals(invocation.getArgument(0)))
                .findFirst());
        when(roundRepository.findByStartTimeAfter(any(LocalDateTime.class))).thenAnswer(invocation -> {
            LocalDateTime startTime = invocation.getArgument(0);
            return savedRounds.stream()
                    .filter(round -> round.getStartTime().isAfter(startTime))
                    .toList();
        });

        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            if (game.getId() == null) {
                game.setId(gameIds.getAndIncrement());
            }
            return game;
        });
        when(pointsRepository.save(any(Points.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRepository.save(any(Break.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            return new RoundReturnDTO(round.getId(), round.getRoundNumber(), round.getStartTime(), round.getEndTime(),
                    round.isFinalGame(), round.isPlayed(), null, round.getBreakTime());
        });

        List<RoundReturnDTO> result = service.createSchedule();

        assertThat(result).hasSize(6);
        assertThat(savedRounds).hasSize(6);
        assertThat(savedRounds.get(5).getBreakTime()).isNotNull();
        verify(adminSettingsUpdateService).updateSettings(any(TournamentDTO.class));
        verify(pointsRepository, atLeastOnce()).save(any(Points.class));
        verify(webSocketService).sendMessage("/topic/rounds", "create");
        verify(adminScheduleUpdateService).sendNotificationForNextRound();
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

    @Test
    void createFinalScheduleCreatesBonusAndFinalRoundsAndTruncatesTeams()
            throws Exception {
        List<Team> finalTeams = buildTeams(5);
        List<Round> savedRounds = new ArrayList<>();
        AtomicLong roundIds = new AtomicLong(1L);

        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);
        when(publicScheduleReadService.getNumberOfRoundsUnplayed()).thenReturn(0);
        when(teamRepository.findByFinalReadyTrue()).thenReturn(finalTeams);
        when(adminRegistrationReadService.getFinalTeams()).thenReturn(finalTeams);
        when(roundRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(savedRounds));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            if (round.getId() == null) {
                round.setId(roundIds.getAndIncrement());
            }
            savedRounds.removeIf(existing -> existing.getId().equals(round.getId()));
            savedRounds.add(round);
            return round;
        });
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pointsRepository.save(any(Points.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            return new RoundReturnDTO(round.getId(), round.getRoundNumber(), round.getStartTime(), round.getEndTime(),
                    round.isFinalGame(), round.isPlayed(), null, round.getBreakTime());
        });

        service.createFinalSchedule();

        verify(gameRepository, times(7)).save(any(Game.class));
        ArgumentCaptor<Points> pointsCaptor = ArgumentCaptor.forClass(Points.class);
        verify(pointsRepository, times(28)).save(pointsCaptor.capture());
        assertThat(pointsCaptor.getAllValues())
                .allMatch(points -> points.getTeam() != null && points.getTeam().getId() <= 4L);
        verify(webSocketService).sendMessage("/topic/rounds", "create");
        verify(adminScheduleUpdateService).sendNotificationForNextRound();
    }

    private Round buildRound(Long id, int roundNumber, boolean finalGame, boolean played) {
        Round round = new Round();
        round.setId(id);
        round.setRoundNumber(roundNumber);
        round.setFinalGame(finalGame);
        round.setPlayed(played);
        round.setStartTime(LocalDateTime.now().plusMinutes(roundNumber));
        round.setEndTime(round.getStartTime().plusMinutes(20));
        return round;
    }

    private List<Team> buildTeams(int count) {
        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Team team = new Team();
            team.setId((long) i);
            team.setTeamName("Team-" + i);
            Character character = new Character();
            character.setCharacterName("Character-" + i);
            team.setCharacter(character);
            teams.add(team);
        }
        return teams;
    }

    private List<List<List<Integer>>> buildSchedulePlan(int rounds, int gamesPerRound) {
        List<List<List<Integer>>> plan = new ArrayList<>();
        for (int roundIndex = 0; roundIndex < rounds; roundIndex++) {
            List<List<Integer>> games = new ArrayList<>();
            for (int gameIndex = 0; gameIndex < gamesPerRound; gameIndex++) {
                games.add(List.of(0, 1, 2, 3));
            }
            plan.add(games);
        }
        return plan;
    }
}
