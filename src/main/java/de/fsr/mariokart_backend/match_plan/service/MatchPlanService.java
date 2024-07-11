package de.fsr.mariokart_backend.match_plan.service;

import de.fsr.mariokart_backend.match_plan.model.dto.*;
import de.fsr.mariokart_backend.match_plan.service.dto.MatchPlanInputDTOService;
import de.fsr.mariokart_backend.match_plan.service.dto.MatchPlanReturnDTOService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.repository.GameRepository;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.repository.PointsRepository;


import de.fsr.mariokart_backend.exception.EntityNotFoundException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class MatchPlanService {

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final TeamRepository teamRepository;

    private final MatchPlanInputDTOService matchPlanInputDTOService;
    private final MatchPlanReturnDTOService matchPlanReturnDTOService;

    public RoundReturnDTO addRound(RoundInputDTO roundCreation) {
        Round round = matchPlanInputDTOService.roundInputDTOToRound(roundCreation);
        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
//        return roundRepository.save(round);
    }

    public GameReturnDTO addGameDTO(GameInputDTO gameCreation) throws EntityNotFoundException{
        Game game = matchPlanInputDTOService.gameInputDTOToGame(gameCreation);
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.save(game));
//        return gameRepository.save(game);
    }

    public GameReturnDTO addGame(Game game) {
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.save(game));
//        return gameRepository.save(game);
    }

    public PointsReturnDTO addPoints(Points points) {
        return matchPlanReturnDTOService.pointsToPointsDTO(pointsRepository.save(points));
//        return pointsRepository.save(points);
    }




    public List<RoundReturnDTO> getRounds() {
        return roundRepository.findAll().stream()
                                        .map(matchPlanReturnDTOService::roundToRoundDTO)
                                        .toList();
//        return roundRepository.findAll();
    }

    public RoundReturnDTO getRoundById(Long roundId) throws EntityNotFoundException{
        return roundRepository  .findById(roundId)
                                .map(matchPlanReturnDTOService::roundToRoundDTO)
                                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
//        return roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
    }

    public List<RoundReturnDTO> getCurrentRounds() {
        List<Round> rounds = roundRepository.findByPlayedFalse();
//        System.out.println(rounds);
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
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID.")));
//        return gameRepository   .findById(gameId)
//                                .map(matchPlanReturnDTOService::gameToGameDTO)
//                                .orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
//        return gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
    }

    public List<PointsReturnDTO> getPoints() {
        return pointsRepository.findAll()   .stream()
                                            .map(matchPlanReturnDTOService::pointsToPointsDTO)
                                            .toList();
    }

    public RoundReturnDTO updateRoundPlayed(Long roundId, RoundInputDTO roundCreation) throws EntityNotFoundException{
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        List<Round> rounds = roundRepository.findByStartTimeAfter(round.getStartTime());
        rounds.sort(Comparator.comparing(Round::getStartTime));

        round.setPlayed(roundCreation.isPlayed());

        if (round.isPlayed() && !rounds.isEmpty()) {
            for (int i = 0; i < rounds.size(); i++) {
                rounds.get(i).setStartTime(round.getStartTime().plusMinutes(20L * i));
                rounds.get(i).setEndTime(round.getStartTime().plusMinutes(20L * i + 20));
                roundRepository.save(rounds.get(i));
            }
        }

        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
//        return roundRepository.save(round)
    }

    public RoundReturnDTO updateRound(Long roundId, RoundInputDTO roundCreation) throws EntityNotFoundException{
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        round.setPlayed(roundCreation.isPlayed());
        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
//        return roundRepository.save(round);
    }

    public GameReturnDTO updateGame(Long gameId, GameInputDTO gameCreation) throws EntityNotFoundException{
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
        game.setSwitchGame(gameCreation.getSwitchGame());
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.save(game));
//        return gameRepository.save(game);
    }

    public PointsReturnDTO updatePoints(Long roundId, Long teamId, Long gameId, PointsInputDTO pointsCreation) throws EntityNotFoundException{
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
//        return pointsRepository.save(points);
    }

    public List<RoundReturnDTO> createMatchPlan() throws EntityNotFoundException, IOException {
        String static_dir = "src/main/resources/static/team_plans/";

        List<Team> teams = teamRepository.findAll();

        if(teams.size()>25 || teams.size()<16) {
            throw new EntityNotFoundException("There need to be 16 to 25 Teams!");
        }
        // Datei öffnen
        String fileName = static_dir + teams.size() + "_teams.txt";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        // ArrayList für das Ergebnis erstellen
        ArrayList<ArrayList<ArrayList<Integer>>> result = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            // Neue Liste für jede Zeile
            ArrayList<ArrayList<Integer>> lineList = new ArrayList<>();

            // Zeile in Sub-Listen teilen
            String[] subLists = line.split(";");
            for (String subList : subLists) {
                // Sub-Liste in Integer konvertieren und zur lineList hinzufügen
                ArrayList<Integer> intList = new ArrayList<>();
                String[] numbers = subList.split(",");
                for (String number : numbers) {
                    intList.add(Integer.parseInt(number));
                }
                lineList.add(intList);
            }

            // lineList zur Ergebnisliste hinzufügen
            result.add(lineList);
        }
        reader.close();

        LocalDateTime currRoundTime = LocalDateTime.now().plusMinutes(5);
        for(ArrayList<ArrayList<Integer>> round_teams : result) {
            Round round = new Round();
            round.setStartTime(currRoundTime);
            currRoundTime = currRoundTime.plusMinutes(18);
            roundRepository.save(round);

            for(int i = 0; i < round_teams.size(); i++) {
                Game game = new Game();
                switch (i) {
                    case 0:
                        game.setSwitchGame("Weiß");
                        break;
                    case 2:
                        game.setSwitchGame("Blau");
                        break;
                    case 3:
                        game.setSwitchGame("Rot");
                        break;
                    case 4:
                        game.setSwitchGame("Grün");
                        break;
                }
                game.setRound(round);
                gameRepository.save(game);
            }

        }
        return roundRepository.findAll().stream()
                .map(matchPlanReturnDTOService::roundToRoundDTO)
                .toList();
    }

    public List<RoundReturnDTO> createFinalPlan() {
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

        for (int i = 0; i < 2; i++){
            createFinalRounds(teams, LocalDateTime.now().plusMinutes(20 * i));
        }

        return roundRepository.findAll().stream()
                                        .map(matchPlanReturnDTOService::roundToRoundDTO)
                                        .toList();
    }

    private void createFinalRounds(List<Team> teams, LocalDateTime start_time) {

        Round round = new Round();
        round.setFinalGame(true);
        round.setPlayed(false);
        round.setStartTime(start_time);
        round.setEndTime(start_time.plusMinutes(40));
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


}
