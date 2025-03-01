package de.fsr.mariokart_backend.match_plan.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.model.Break;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.MatchPlanDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.repository.BreakRepository;
import de.fsr.mariokart_backend.match_plan.repository.GameRepository;
import de.fsr.mariokart_backend.match_plan.repository.PointsRepository;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.match_plan.service.dto.MatchPlanInputDTOService;
import de.fsr.mariokart_backend.match_plan.service.dto.MatchPlanReturnDTOService;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class MatchPlanService {

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final BreakRepository breakRepository;
    private final TeamRepository teamRepository;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final MatchPlanInputDTOService matchPlanInputDTOService;
    private final MatchPlanReturnDTOService matchPlanReturnDTOService;
    private final SettingsService settingsService;
    private final WebSocketService webSocketService;

    public RoundReturnDTO addRound(RoundInputDTO roundCreation) {
        Round round = matchPlanInputDTOService.roundInputDTOToRound(roundCreation);
        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
    }

    public GameReturnDTO addGameDTO(GameInputDTO gameCreation) throws EntityNotFoundException{
        Game game = matchPlanInputDTOService.gameInputDTOToGame(gameCreation);
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.save(game));
    }

    public GameReturnDTO addGame(Game game) {
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.save(game));
    }

    public PointsReturnDTO addPoints(Points points) {
        return matchPlanReturnDTOService.pointsToPointsDTO(pointsRepository.save(points));
    }

    public BreakReturnDTO addBreak(BreakInputDTO breakCreation) throws EntityNotFoundException {
        Break aBreak = breakRepository.save(matchPlanInputDTOService.breakInputDTOToBreak(breakCreation));
        Round round = roundRepository.findById(breakCreation.getRoundId()).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        if (round.isFinalGame()){
            throw new IllegalArgumentException("Final game has no breaks");
        }

        round.setBreakTime(aBreak);
        roundRepository.save(round);
        return matchPlanReturnDTOService.breakToBreakDTO(round.getBreakTime());
    }

    public List<RoundReturnDTO> getRounds() {
        return roundRepository.findAll().stream()
                                        .map(matchPlanReturnDTOService::roundToRoundDTO)
                                        .toList();
    }

    public RoundReturnDTO getRoundById(Long roundId) throws EntityNotFoundException{
        return roundRepository.findById(roundId)
                              .map(matchPlanReturnDTOService::roundToRoundDTO)
                              .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
    }

    public List<RoundReturnDTO> getCurrentRounds() {
        List<Round> rounds = roundRepository.findByPlayedFalse();

        rounds.sort(Comparator.comparing(Round::getStartTime));
        if (rounds.size() > 2){
            rounds = rounds.subList(0, 2);
        }
        return rounds   .stream()
                        .map(matchPlanReturnDTOService::roundToRoundDTO)
                        .toList();
    }

    public List<GameReturnDTO> getGamesByRoundId(Long gameId) {
        return gameRepository   .findByRoundId(gameId).stream()
                                .map(matchPlanReturnDTOService::gameToGameDTO)
                                .toList();
    }

    public List<GameReturnDTO> getGames() {
        return gameRepository.findAll() .stream()
                                        .map(matchPlanReturnDTOService::gameToGameDTO)
                                        .toList();
    }

    public GameReturnDTO getGameById(Long gameId) throws EntityNotFoundException{
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.findById(gameId)
                                                                     .orElseThrow(() -> new EntityNotFoundException("There is no game with this ID.")));
    }

    public List<PointsReturnDTO> getPoints() {
        return pointsRepository.findAll()   .stream()
                                            .map(matchPlanReturnDTOService::pointsToPointsDTO)
                                            .toList();
    }

    public BreakReturnDTO getBreak() {
        return matchPlanReturnDTOService.breakToBreakDTO(breakRepository.findAll().get(0));
    }

    public RoundReturnDTO updateRoundPlayed(Long roundId, RoundInputDTO roundCreation) throws EntityNotFoundException{
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        long playMinutes = 20L;
        List<Round> rounds = roundRepository.findByStartTimeAfter(round.getStartTime());
        List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
        rounds.sort(Comparator.comparing(Round::getStartTime));
        notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));

        round.setPlayed(roundCreation.isPlayed());

        round.setEndTime(LocalDateTime.now());

//        if (round.isPlayed() && !rounds.isEmpty()) {
//            for (int i = 0; i < rounds.size(); i++) {
//                rounds.get(i).setStartTime(LocalDateTime.now().plusMinutes(playMinutes * i));
//                rounds.get(i).setEndTime(LocalDateTime.now().plusMinutes(playMinutes * i).plusMinutes(playMinutes));
//                roundRepository.save(rounds.get(i));
//            }
//        }

        if (round.isPlayed() && !notPlayedRounds.isEmpty()) {
            notPlayedRounds.remove(round);
        }

        if (!round.isPlayed() && !notPlayedRounds.contains(round)){
            notPlayedRounds.add(round);
            notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));
        }

        for (int i = 0; i < notPlayedRounds.size(); i++) {
            notPlayedRounds.get(i).setStartTime(LocalDateTime.now().plusMinutes(playMinutes * i));
            notPlayedRounds.get(i).setEndTime(LocalDateTime.now().plusMinutes(playMinutes * i).plusMinutes(playMinutes));
            roundRepository.save(notPlayedRounds.get(i));
        }

        Round breakRound = notPlayedRounds.stream()
                .filter(r -> r.getBreakTime() != null)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No round with a break found."));

        List<Round> roundsAfterBreak = notPlayedRounds.stream()
                .filter(r -> r.getStartTime().isAfter(breakRound.getStartTime()))
                .toList();

        int breakDuration = (int) Duration.between(breakRound.getBreakTime().getStartTime(), breakRound.getBreakTime().getEndTime()).toMinutes();

        System.out.println(breakDuration);
        System.out.println(breakRound.getStartTime());


        breakRound.getBreakTime().setStartTime(breakRound.getStartTime());
        breakRound.getBreakTime().setEndTime(breakRound.getStartTime().plusMinutes(breakDuration));

        breakRound.setStartTime(breakRound.getBreakTime().getEndTime());
        breakRound.setEndTime(breakRound.getStartTime().plusMinutes(playMinutes));

        System.out.println(breakRound.getStartTime());

        roundRepository.save(breakRound);
        breakRepository.save(breakRound.getBreakTime());

        for (int i = 0; i < roundsAfterBreak.size(); i++) {
            roundsAfterBreak.get(i).setStartTime(breakRound.getEndTime().plusMinutes(playMinutes * i));
            roundsAfterBreak.get(i).setEndTime(breakRound.getEndTime().plusMinutes(playMinutes * i).plusMinutes(playMinutes));
            roundRepository.save(roundsAfterBreak.get(i));
        }

//      TODO: enum for possible send/receive topics
        webSocketService.sendMessage("/topic/rounds", "update");

        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
    }

    public RoundReturnDTO updateRound(Long roundId, RoundInputDTO roundCreation) throws EntityNotFoundException{
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        round.setPlayed(roundCreation.isPlayed());
        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
    }

    public GameReturnDTO updateGame(Long gameId, GameInputDTO gameCreation) throws EntityNotFoundException{
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
        game.setSwitchGame(gameCreation.getSwitchGame());
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.save(game));
    }

    public PointsReturnDTO updatePoints(Long roundId, Long gameId, Long teamId, PointsInputDTO pointsCreation) throws EntityNotFoundException{
        Points points = pointsRepository.findByGameIdAndTeamId(gameId, teamId).orElseThrow(() -> new EntityNotFoundException("There are no points with this ID."));
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

        if (round.isFinalGame()){
            points.setFinalPoints(pointsCreation.getPoints());
        } else {
            points.setGroupPoints(pointsCreation.getPoints());
        }

        points.setGame(game);
        points.setTeam(team);
        return matchPlanReturnDTOService.pointsToPointsDTO(pointsRepository.save(points));
    }

    public BreakReturnDTO updateBreak(BreakInputDTO breakCreation) throws EntityNotFoundException {
        if (!isMatchPlanCreated()){
            throw new EntityNotFoundException("Match plan not created yet.");
        }

        Break aBreak = breakRepository.findAll().get(0);
        Round oldRound = aBreak.getRound();
        Round newRound = roundRepository.findById(breakCreation.getRoundId()).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        aBreak.setRound(newRound);
        aBreak.setStartTime(newRound.getStartTime().minusMinutes(breakCreation.getBreakDuration()));
        aBreak.setEndTime(newRound.getStartTime());
        if (breakCreation.getBreakEnded() != null){
            aBreak.setBreakEnded(breakCreation.getBreakEnded());
        }
        oldRound.setBreakTime(null);
        roundRepository.save(oldRound);
        Break newBreak = breakRepository.save(aBreak);
        newRound.setBreakTime(newBreak);
        roundRepository.save(newRound);
        return matchPlanReturnDTOService.breakToBreakDTO(newRound.getBreakTime());
    }

    public List<RoundReturnDTO> createFinalPlan() throws RoundsAlreadyExistsException, NotEnoughTeamsException {

        if (!roundRepository.findByFinalGameTrue().isEmpty()){
            throw new RoundsAlreadyExistsException("Final plan already created");
        } else if (!roundRepository.findByPlayedFalse().isEmpty()){
            throw new IllegalArgumentException("Not all rounds played");
        } else if (teamRepository.findByFinalReadyTrue().size() < 4){
            throw new NotEnoughTeamsException("Not enough teams ready for final");
        }

        List<Team> teams = teamRepository.findByFinalReadyTrue();

        teams.sort((t1, t2) -> {
            int t1Points = t1.getPoints().stream().mapToInt(Points::getGroupPoints).sum();
            int t2Points = t2.getPoints().stream().mapToInt(Points::getGroupPoints).sum();
            return t2Points - t1Points;
        });

        if (teams.size() > 4){
            teams = teams.subList(0, 4);
        }

        Round round = new Round();
        round.setFinalGame(true);
        round.setPlayed(true);
        round.setStartTime(LocalDateTime.now());
        round.setEndTime(LocalDateTime.now());
        roundRepository.save(round);

        Game game = new Game();
        game.setSwitchGame("Blau");
        game.setRound(round);
        gameRepository.save(game);
        for (int i = 0; i < teams.size(); i++){
            Points point = new Points();
            point.setGroupPoints(0);
            point.setFinalPoints(3-i);
            point.setTeam(teams.get(i));
            point.setGame(game);
            pointsRepository.save(point);
        }

        for (int i = 0; i < 1; i++){
            createFinalRounds(teams, LocalDateTime.now().plusMinutes(20 * i));
        }

        webSocketService.sendMessage("/topic/rounds", "create");

        return roundRepository.findAll().stream()
                                        .map(matchPlanReturnDTOService::roundToRoundDTO)
                                        .toList();
    }

    private void createFinalRounds(List<Team> teams, LocalDateTime start_time) {

        Round round = new Round();
        round.setFinalGame(true);
        round.setPlayed(false);
        round.setStartTime(start_time);
        round.setEndTime(start_time.plusMinutes(20));
        roundRepository.save(round);

        for (int i = 0; i < 4; i++){
            Game game = new Game();
            game.setSwitchGame("Blau");
            game.setRound(round);
            gameRepository.save(game);
            for (Team team : teams){
                Points point = new Points();
                point.setGroupPoints(0);
                point.setFinalPoints(0);
                point.setTeam(team);
                point.setGame(game);
                pointsRepository.save(point);
            }
        }
    }

    public Boolean isMatchPlanCreated() {
        return !roundRepository.findAll().isEmpty();
    }

    public Boolean isFinalPlanCreated() {
        return !roundRepository.findByFinalGameTrue().isEmpty();
    }

    public void deleteMatchPlan() {
        roundRepository.deleteAll();
//        gameRepository.deleteAll();
//        pointsRepository.deleteAll();
    }

    public void deleteFinalPlan() {
        roundRepository.deleteAll(roundRepository.findByFinalGameTrue());
//        gameRepository.deleteAll(gameRepository.findByRoundFinalGameTrue());
//        pointsRepository.deleteAll(pointsRepository.findByGameRoundFinalGameTrue());
    }

    public List<RoundReturnDTO> createMatchPlan() throws RoundsAlreadyExistsException, NotEnoughTeamsException, EntityNotFoundException {
        if (!roundRepository.findAll().isEmpty()){
            throw new RoundsAlreadyExistsException("Match plan already created");
        } else if (teamRepository.findAll().size() < 16){
            throw new NotEnoughTeamsException("Not enough teams");
        }

        int teamCount = teamRepository.findAll().size();
        MatchPlanDTO response = getGeneratedMatchPlan(teamCount);

        if (!roundRepository.findAll().isEmpty()){
            throw new RoundsAlreadyExistsException("Match plan already created");
        }

        System.out.println(response);
        List<List<List<Integer>>> plan = response.getPlan();
        int maxGamesCount = response.getMax_games_count();
        List<String> switchColors = List.of("Blau", "Rot", "Grün", "Weiß");

        for (int i = 0; i < plan.size(); i++) {
            Round round = new Round();
            round.setPlayed(false);
            round.setStartTime(LocalDateTime.now().plusMinutes(20L * i));
            round.setEndTime(LocalDateTime.now().plusMinutes(20L * i).plusMinutes(20));
            roundRepository.save(round);

            for (int j = 0; j < plan.get(i).size(); j++) {
                Game game = new Game();
                game.setSwitchGame(switchColors.get(j));
                game.setRound(round);
                gameRepository.save(game);

                for (int k = 0; k < plan.get(i).get(j).size(); k++) {
                    Points point = new Points();
                    point.setGroupPoints(0);
                    point.setFinalPoints(0);
//                    System.out.println(response.get(i).get(j).get(k));
                    point.setTeam(teamRepository.findById((long) (plan.get(i).get(j).get(k) + 1)).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID.")));
                    point.setGame(game);
                    pointsRepository.save(point);
                }
            }
        }

        List<Round> rounds = roundRepository.findAll();
        addBreak(new BreakInputDTO(rounds.get(5).getId(), 30, false));

        List<Round> roundsAfterBreak = roundRepository.findByStartTimeAfter(rounds.get(5).getStartTime().minusMinutes(1));

        for (int i = 0; i < roundsAfterBreak.size(); i++) {
            roundsAfterBreak.get(i).setStartTime(rounds.get(5).getStartTime().plusMinutes(20L * i).plusMinutes(i == 0 ? 30 : 0));
            roundsAfterBreak.get(i).setEndTime(rounds.get(5).getStartTime().plusMinutes(20L * i).plusMinutes(20));
            roundRepository.save(roundsAfterBreak.get(i));
        }


        settingsService.updateSettings(new TournamentDTO(null, false, maxGamesCount));
        webSocketService.sendMessage("/topic/rounds", "create");

        return roundRepository.findAll().stream()
                                        .map(matchPlanReturnDTOService::roundToRoundDTO)
                                        .toList();

    }

    public MatchPlanDTO getGeneratedMatchPlan(int teamCount) {
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("num_teams", teamCount);
        Mono<String> response = webClient.post()
                .uri("/match_plan")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);

        try {
            return objectMapper.readValue(response.block(), MatchPlanDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }
}
