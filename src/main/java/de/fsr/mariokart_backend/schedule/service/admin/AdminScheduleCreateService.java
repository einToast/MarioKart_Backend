package de.fsr.mariokart_backend.schedule.service.admin;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
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
import de.fsr.mariokart_backend.schedule.model.dto.ScheduleInputDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.GameRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleInputDTOService;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = {"schedule", "registration"})
@CacheEvict(allEntries = true)
@Transactional(rollbackFor = Exception.class)
public class AdminScheduleCreateService {

    public static final int MAX_FIELDS = 16;
    public static final int MIN_TEAMS_PER_GAME = 2;
    public static final int MAX_TEAMS_PER_GAME = 8;
    public static final int DEFAULT_BREAK_ROUND = 6;
    public static final int DEFAULT_BREAK_MINUTES = 30;

    // The final is played on the first switch
    private static final int MAIN_SWITCH_INDEX = 0;
    private static final int LEGACY_SCHEDULER_VERSION = 1;
    // First scheduler that takes a number of teams for every field
    private static final int PER_FIELD_SCHEDULER_VERSION = 4;

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final BreakRepository breakRepository;
    private final TeamRepository teamRepository;

    private final AdminScheduleUpdateService adminScheduleUpdateService;
    private final AdminSettingsUpdateService adminSettingsUpdateService;
    private final AdminRegistrationReadService adminRegistrationReadService;
    private final PublicScheduleReadService publicScheduleReadService;
    private final PublicSettingsReadService publicSettingsReadService;
    private final ScheduleInputDTOService scheduleInputDTOService;
    private final ScheduleReturnDTOService scheduleReturnDTOService;
    private final WebSocketService webSocketService;
    private final RestClient scheduleRestClient;
    private final ObjectMapper objectMapper;

    public RoundReturnDTO addRound(RoundInputDTO roundCreation) {
        Round round = scheduleInputDTOService.roundInputDTOToRound(roundCreation);
        if (round.getRoundNumber() == 0) {
            round.setRoundNumber((int) roundRepository.count() + 1);
        }
        return scheduleReturnDTOService.roundToRoundDTO(roundRepository.save(round));
    }

    public Round addRound(Round round) {
        return roundRepository.save(round);
    }

    public Game addGame(Game game) {
        return gameRepository.save(game);
    }

    public Points addPoints(Points points) {
        return pointsRepository.save(points);
    }

    public BreakReturnDTO addBreak(BreakInputDTO breakCreation) throws EntityNotFoundException {
        Break aBreak = breakRepository.save(scheduleInputDTOService.breakInputDTOToBreak(breakCreation));
        Round round = roundRepository.findById(breakCreation.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        if (round.isFinalGame()) {
            throw new IllegalArgumentException("Final game has no breaks");
        }

        round.setBreakTime(aBreak);
        addRound(round);
        return scheduleReturnDTOService.breakToBreakDTO(round.getBreakTime());
    }

    public List<RoundReturnDTO> createFinalSchedule()
            throws RoundsAlreadyExistsException, NotEnoughTeamsException {
        if (!publicScheduleReadService.isScheduleCreated()) {
            throw new RoundsAlreadyExistsException("Schedule not created");
        } else if (publicScheduleReadService.isFinalScheduleCreated()) {
            throw new RoundsAlreadyExistsException("Final schedule already created");
        } else if (publicScheduleReadService.getNumberOfRoundsUnplayed() > 0) {
            throw new IllegalArgumentException("Not all rounds played");
        }

        TournamentDTO settings = publicSettingsReadService.getSettings();
        int finalTeamsCount = settings.finalTeamsCountOrDefault();
        if (teamRepository.findByFinalReadyTrue().size() < finalTeamsCount) {
            throw new NotEnoughTeamsException("Not enough teams ready for final");
        }

        List<Team> teams = adminRegistrationReadService.getFinalTeams();

        if (teams.size() > finalTeamsCount) {
            teams = teams.subList(0, finalTeamsCount);
        }

        int roundMinutes = settings.roundMinutesOrDefault();
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        createFinalBonusRound(teams, now);

        int switchCount = gameRepository.findFirstByRound_FinalGameFalseOrderBySwitchIndexDesc()
                .map(game -> game.getSwitchIndex() + 1)
                .orElse(1);
        List<Integer> gamesPerRound = splitFinalGames(settings.finalGamesCountOrDefault(), switchCount);
        for (int i = 0; i < gamesPerRound.size(); i++) {
            createFinalRound(teams, now.plusMinutes((long) roundMinutes * i), roundMinutes, gamesPerRound.get(i));
        }

        webSocketService.sendMessage("/topic/rounds", "create");
        adminScheduleUpdateService.sendNotificationForNextRound();

        return roundRepository.findAll().stream()
                .map(scheduleReturnDTOService::roundToRoundDTO)
                .toList();
    }

    static List<Integer> splitFinalGames(int gamesCount, int switchCount) {
        int rounds = Math.ceilDiv(gamesCount, switchCount);
        List<Integer> gamesPerRound = new ArrayList<>();
        for (int i = 0; i < rounds; i++) {
            gamesPerRound.add(gamesCount / rounds + (i < gamesCount % rounds ? 1 : 0));
        }
        return gamesPerRound;
    }

    private void createFinalBonusRound(List<Team> teams, LocalDateTime startTime) {
        Round round = new Round();
        round.setRoundNumber((int) roundRepository.count() + 1);
        round.setFinalGame(true);
        round.setPlayed(true);
        round.setStartTime(startTime);
        round.setEndTime(startTime);
        addRound(round);
        for (int i = 0; i < 1; i++) {
            Game game = new Game();
            game.setSwitchIndex(MAIN_SWITCH_INDEX);
            game.setRound(round);
            addGame(game);
            for (int j = 0; j < teams.size(); j++) {
                Points point = new Points();
                point.setGroupPoints(0);
                point.setFinalPoints(teams.size() - j);
                point.setTeam(teams.get(j));
                point.setGame(game);
                addPoints(point);
            }
        }
    }

    private void createFinalRound(List<Team> teams, LocalDateTime startTime, int roundMinutes, int gamesCount) {
        Round round = new Round();
        round.setRoundNumber((int) roundRepository.count() + 1);
        round.setFinalGame(true);
        round.setPlayed(false);
        round.setStartTime(startTime);
        round.setEndTime(startTime.plusMinutes(roundMinutes));
        addRound(round);
        for (int i = 0; i < gamesCount; i++) {
            Game game = new Game();
            game.setSwitchIndex(MAIN_SWITCH_INDEX);
            game.setRound(round);
            addGame(game);
            for (Team team : teams) {
                Points point = new Points();
                point.setGroupPoints(0);
                point.setFinalPoints(0);
                point.setTeam(team);
                point.setGame(game);
                addPoints(point);
            }
        }
    }

    public List<RoundReturnDTO> createSchedule(ScheduleInputDTO scheduleCreation)
            throws RoundsAlreadyExistsException, NotEnoughTeamsException, EntityNotFoundException,
            NotificationNotSentException, IllegalArgumentException {
        validateScheduleCreation(scheduleCreation);

        int numTeams = (int) teamRepository.count();
        ScheduleDTO scheduleDTO = getGeneratedSchedule(scheduleCreation, numTeams);

        validateScheduleCreation(scheduleCreation);

        int roundMinutes = publicSettingsReadService.getSettings().roundMinutesOrDefault();
        createRoundsAndGames(scheduleDTO, roundMinutes);
        addBreakAndUpdateTimes(breakRoundOrDefault(scheduleCreation), breakMinutesOrDefault(scheduleCreation),
                roundMinutes);
        updateTournamentSettings(scheduleDTO.getMaxGamesCount());

        webSocketService.sendMessage("/topic/rounds", "create");
        adminScheduleUpdateService.sendNotificationForNextRound();

        return roundRepository.findAll().stream()
                .map(scheduleReturnDTOService::roundToRoundDTO)
                .toList();
    }

    private void validateScheduleCreation(ScheduleInputDTO scheduleCreation)
            throws RoundsAlreadyExistsException, NotEnoughTeamsException {
        if (publicScheduleReadService.isScheduleCreated()) {
            throw new RoundsAlreadyExistsException("Schedule already created");
        }
        if (breakRoundOrDefault(scheduleCreation) < 1) {
            throw new IllegalArgumentException("Invalid break round");
        }
        if (breakMinutesOrDefault(scheduleCreation) < 1) {
            throw new IllegalArgumentException("Invalid break length");
        }
        if (scheduleCreation.getVersion() == LEGACY_SCHEDULER_VERSION) {
            // The first scheduler only knows four fields with four teams each
            if (teamRepository.count() < 16) {
                throw new NotEnoughTeamsException("Not enough teams");
            }
        } else {
            validateFields(scheduleCreation);
        }
    }

    private void validateFields(ScheduleInputDTO scheduleCreation) throws NotEnoughTeamsException {
        if (scheduleCreation.getNumFields() <= 0 || scheduleCreation.getNumFields() > MAX_FIELDS
                || scheduleCreation.getNumRounds() <= 0) {
            throw new IllegalArgumentException("Invalid schedule parameters");
        }
        List<Integer> teamsPerField = resolveTeamsPerField(scheduleCreation);
        if (teamsPerField.size() != scheduleCreation.getNumFields()
                || teamsPerField.stream().anyMatch(teamCount -> teamCount == null
                        || teamCount < MIN_TEAMS_PER_GAME || teamCount > MAX_TEAMS_PER_GAME)) {
            throw new IllegalArgumentException("Invalid schedule parameters");
        }
        // A team can only play on one field per round, so every field needs its own
        // teams
        int teamsPerRound = teamsPerField.stream().mapToInt(Integer::intValue).sum();
        if (teamRepository.count() < teamsPerRound) {
            throw new NotEnoughTeamsException(
                    "Not enough teams: fields with %s teams need at least %d teams".formatted(
                            teamsPerField, teamsPerRound));
        }
    }

    private static int breakRoundOrDefault(ScheduleInputDTO scheduleCreation) {
        return scheduleCreation.getBreakRound() != null ? scheduleCreation.getBreakRound() : DEFAULT_BREAK_ROUND;
    }

    private static int breakMinutesOrDefault(ScheduleInputDTO scheduleCreation) {
        return scheduleCreation.getBreakMinutes() != null ? scheduleCreation.getBreakMinutes() : DEFAULT_BREAK_MINUTES;
    }

    private boolean hasTeamsPerField(ScheduleInputDTO scheduleCreation) {
        return scheduleCreation.getTeamsPerField() != null && !scheduleCreation.getTeamsPerField().isEmpty();
    }

    // One entry per field. Without a list every field gets the same number of teams
    private List<Integer> resolveTeamsPerField(ScheduleInputDTO scheduleCreation) {
        if (!hasTeamsPerField(scheduleCreation)) {
            return Collections.nCopies(scheduleCreation.getNumFields(), scheduleCreation.getTeamsPerGame());
        }
        if (scheduleCreation.getVersion() < PER_FIELD_SCHEDULER_VERSION) {
            throw new IllegalArgumentException("Invalid schedule parameters");
        }
        return scheduleCreation.getTeamsPerField();
    }

    private void createRoundsAndGames(ScheduleDTO scheduleDTO, int roundMinutes) throws EntityNotFoundException {
        List<List<List<Integer>>> plan = scheduleDTO.getPlan();
        List<Team> teams = teamRepository.findAll();

        for (int roundIndex = 0; roundIndex < plan.size(); roundIndex++) {
            Round round = createRound(roundIndex, roundMinutes);

            for (int gameIndex = 0; gameIndex < plan.get(roundIndex).size(); gameIndex++) {
                Game game = createGame(round, gameIndex);
                createPointsForGame(plan.get(roundIndex).get(gameIndex), game, teams);
            }
        }
    }

    private Round createRound(int roundIndex, int roundMinutes) {
        Round round = new Round();
        round.setRoundNumber(roundIndex + 1);
        round.setPlayed(false);
        LocalDateTime startTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes((long) roundMinutes * roundIndex);
        round.setStartTime(startTime);
        round.setEndTime(startTime.plusMinutes(roundMinutes));
        return addRound(round);
    }

    private Game createGame(Round round, int switchIndex) {
        Game game = new Game();
        game.setSwitchIndex(switchIndex);
        game.setRound(round);
        return addGame(game);
    }

    private void createPointsForGame(List<Integer> teamIndices, Game game, List<Team> teams) {
        for (Integer teamIndex : teamIndices) {
            Points point = new Points();
            point.setGroupPoints(0);
            point.setFinalPoints(0);
            point.setTeam(teams.get(teamIndex));
            point.setGame(game);
            addPoints(point);
        }
    }

    private void addBreakAndUpdateTimes(int breakRoundNumber, int breakMinutes, int roundMinutes)
            throws EntityNotFoundException {
        List<Round> rounds = new ArrayList<>(roundRepository.findAll());
        rounds.sort(Comparator.comparing(Round::getRoundNumber));
        // Short schedules have their break before the last round
        Round breakRound = rounds.get(Math.min(breakRoundNumber - 1, rounds.size() - 1));
        LocalDateTime breakEndTime = breakRound.getStartTime().plusMinutes(breakMinutes);
        addBreak(new BreakInputDTO(breakRound.getId(), breakMinutes, false));

        updateRoundTimesAfterBreak(rounds.subList(rounds.indexOf(breakRound), rounds.size()), breakEndTime,
                roundMinutes);
    }

    private void updateRoundTimesAfterBreak(List<Round> roundsFromBreak, LocalDateTime breakEndTime,
            int roundMinutes) {
        LocalDateTime startTime = breakEndTime;
        for (Round round : roundsFromBreak) {
            round.setStartTime(startTime);
            round.setEndTime(startTime.plusMinutes(roundMinutes));
            addRound(round);
            startTime = round.getEndTime();
        }
    }

    private void updateTournamentSettings(int maxGamesCount) throws RoundsAlreadyExistsException {
        adminSettingsUpdateService.updateSettings(new TournamentDTO(null, false, maxGamesCount, null));
    }

    private ScheduleDTO getGeneratedSchedule(ScheduleInputDTO scheduleCreation, int numTeams) {
        boolean perField = scheduleCreation.getVersion() >= PER_FIELD_SCHEDULER_VERSION
                && hasTeamsPerField(scheduleCreation);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", scheduleCreation.getVersion());
        requestBody.put("num_teams", numTeams);
        requestBody.put("num_fields", scheduleCreation.getNumFields());
        requestBody.put("num_rounds", scheduleCreation.getNumRounds());
        requestBody.put("num_teams_per_game",
                perField ? scheduleCreation.getTeamsPerField() : scheduleCreation.getTeamsPerGame());
        String response;
        try {
            response = scheduleRestClient.post()
                    .uri("/schedule")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to schedule generator", e);
        }

        try {
            return objectMapper.readValue(response, ScheduleDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }
}