package de.fsr.mariokart_backend.schedule.service.admin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.MatchPlanDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
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
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class AdminScheduleCreateService {

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final BreakRepository breakRepository;
    private final TeamRepository teamRepository;

    private final AdminSettingsUpdateService settingsUpdateService;
    private final PublicScheduleReadService publicScheduleReadService;
    private final ScheduleInputDTOService scheduleInputDTOService;
    private final ScheduleReturnDTOService scheduleReturnDTOService;
    private final WebSocketService webSocketService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public RoundReturnDTO addRound(RoundInputDTO roundCreation) {
        Round round = scheduleInputDTOService.roundInputDTOToRound(roundCreation);
        if (round.getRoundNumber() == 0) {
            round.setRoundNumber(roundRepository.findAll().size() + 1);
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

    public List<RoundReturnDTO> createFinalPlan() throws RoundsAlreadyExistsException, NotEnoughTeamsException {
        if (!publicScheduleReadService.isMatchPlanCreated()) {
            throw new RoundsAlreadyExistsException("Match schedule not created");
        } else if (publicScheduleReadService.isFinalPlanCreated()) {
            throw new RoundsAlreadyExistsException("Final schedule already created");
        } else if (publicScheduleReadService.getNumberOfRoundsUnplayed() > 0) {
            throw new IllegalArgumentException("Not all rounds played");
        } else if (teamRepository.findByFinalReadyTrue().size() < 4) {
            throw new NotEnoughTeamsException("Not enough teams ready for final");
        }

        List<Team> teams = teamRepository.findByFinalReadyTrue();
        teams.sort((t1, t2) -> {
            int t1Points = t1.getPoints().stream().mapToInt(Points::getGroupPoints).sum();
            int t2Points = t2.getPoints().stream().mapToInt(Points::getGroupPoints).sum();
            return t2Points - t1Points;
        });

        if (teams.size() > 4) {
            teams = teams.subList(0, 4);
        }

        for (int i = 0; i < 1; i++) {
            createFinalRounds(teams, LocalDateTime.now().plusMinutes(20L * i));
        }

        webSocketService.sendMessage("/topic/rounds", "create");

        return roundRepository.findAll().stream()
                .map(scheduleReturnDTOService::roundToRoundDTO)
                .toList();
    }

    private void createFinalRounds(List<Team> teams, LocalDateTime startTime) {
        Round round = new Round();
        round.setRoundNumber(roundRepository.findAll().size() + 1);
        round.setFinalGame(true);
        round.setPlayed(false);
        round.setStartTime(startTime);
        round.setEndTime(startTime.plusMinutes(20L));
        addRound(round);

        for (int i = 0; i < 4; i++) {
            Game game = new Game();
            game.setSwitchGame("Blau");
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

    public List<RoundReturnDTO> createMatchPlan()
            throws RoundsAlreadyExistsException, NotEnoughTeamsException, EntityNotFoundException {
        validateMatchPlanCreation();

        int teamCount = teamRepository.findAll().size();
        MatchPlanDTO matchPlanDTO = getGeneratedMatchPlan(teamCount);

        validateMatchPlanCreation();

        createRoundsAndGames(matchPlanDTO);
        addBreakAndUpdateTimes();
        updateTournamentSettings(matchPlanDTO.getMax_games_count());

        webSocketService.sendMessage("/topic/rounds", "create");

        return roundRepository.findAll().stream()
                .map(scheduleReturnDTOService::roundToRoundDTO)
                .toList();
    }

    private void validateMatchPlanCreation() throws RoundsAlreadyExistsException, NotEnoughTeamsException {
        if (publicScheduleReadService.isMatchPlanCreated()) {
            throw new RoundsAlreadyExistsException("Match schedule already created");
        }
        if (teamRepository.findAll().size() < 16) {
            throw new NotEnoughTeamsException("Not enough teams");
        }
    }

    private void createRoundsAndGames(MatchPlanDTO matchPlanDTO) throws EntityNotFoundException {
        List<List<List<Integer>>> plan = matchPlanDTO.getPlan();
        List<String> switchColors = List.of("Blau", "Rot", "Grün", "Weiß");

        for (int roundIndex = 0; roundIndex < plan.size(); roundIndex++) {
            Round round = createRound(roundIndex);

            for (int gameIndex = 0; gameIndex < plan.get(roundIndex).size(); gameIndex++) {
                Game game = createGame(round, switchColors.get(gameIndex));
                createPointsForGame(plan.get(roundIndex).get(gameIndex), game);
            }
        }
    }

    private Round createRound(int roundIndex) {
        Round round = new Round();
        round.setRoundNumber(roundIndex + 1);
        round.setPlayed(false);
        round.setStartTime(LocalDateTime.now().plusMinutes(20L * roundIndex));
        round.setEndTime(LocalDateTime.now().plusMinutes(20L * roundIndex).plusMinutes(20L));
        return addRound(round);
    }

    private Game createGame(Round round, String switchColor) {
        Game game = new Game();
        game.setSwitchGame(switchColor);
        game.setRound(round);
        return addGame(game);
    }

    private void createPointsForGame(List<Integer> teamIndices, Game game) throws EntityNotFoundException {
        for (Integer teamIndex : teamIndices) {
            Points point = new Points();
            point.setGroupPoints(0);
            point.setFinalPoints(0);
            point.setTeam(teamRepository.findById((long) (teamIndex + 1))
                    .orElseThrow(() -> new EntityNotFoundException("There is no team with this ID.")));
            point.setGame(game);
            addPoints(point);
        }
    }

    private void addBreakAndUpdateTimes() throws EntityNotFoundException {
        List<Round> rounds = roundRepository.findAll();
        addBreak(new BreakInputDTO(rounds.get(5).getId(), 30, false));

        List<Round> roundsAfterBreak = roundRepository
                .findByStartTimeAfter(rounds.get(5).getStartTime().minusMinutes(1));
        updateRoundTimesAfterBreak(rounds.get(5), roundsAfterBreak);
    }

    private void updateRoundTimesAfterBreak(Round breakRound, List<Round> roundsAfterBreak) {
        for (int i = 0; i < roundsAfterBreak.size(); i++) {
            Round round = roundsAfterBreak.get(i);
            LocalDateTime startTime = breakRound.getStartTime().plusMinutes(20L * i).plusMinutes(i == 0 ? 30 : 0);
            round.setStartTime(startTime);
            round.setEndTime(startTime.plusMinutes(20L));
            addRound(round);
        }
    }

    private void updateTournamentSettings(int maxGamesCount) throws RoundsAlreadyExistsException {
        settingsUpdateService.updateSettings(new TournamentDTO(null, false, maxGamesCount));
    }

    private MatchPlanDTO getGeneratedMatchPlan(int teamCount) {
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("num_teams", teamCount);
        Mono<String> response;
        try {
            response = webClient.post()
                    .uri("/match_plan")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to match schedule generator", e);
        }

        try {
            return objectMapper.readValue(response.block(), MatchPlanDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }
}