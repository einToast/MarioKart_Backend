package de.fsr.mariokart_backend.match_plan.service;

import de.fsr.mariokart_backend.match_plan.model.dto.GameDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundDTO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MatchPlanService {

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final TeamRepository teamRepository;

    public Round addRound(RoundDTO roundCreation) {
        Round round = new Round();
        round.setPlayed(roundCreation.isPlayed());
        return roundRepository.save(round);
    }

    public Game addGameDTO(GameDTO gameCreation) throws EntityNotFoundException{
        Game game = new Game();
        game.setSwitchGame(gameCreation.getSwitchGame());
        game.setRound(roundRepository.findById(gameCreation.getRoundId()).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID.")));
        return gameRepository.save(game);
    }

    public Game addGame(Game game) {
        return gameRepository.save(game);
    }

    public Points addPoints(Points points) {
        return pointsRepository.save(points);
    }




    public List<Round> getRounds() {
        return roundRepository.findAll();
    }

    public Round getRoundById(Long roundId) throws EntityNotFoundException{
        return roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
    }

    public List<Game> getGamesByRoundId(Long gameId) {
        return gameRepository.findByRoundId(gameId);
    }

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long gameId) throws EntityNotFoundException{
        return gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
    }

    public List<Points> getPoints() {
        return pointsRepository.findAll();
    }

    public Round updateRoundPlayed(Long roundId, RoundDTO roundCreation) throws EntityNotFoundException{
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        List<Round> rounds = roundRepository.findByStartTimeAfter(round.getStartTime());
        rounds.sort(Comparator.comparing(Round::getStartTime));

        round.setPlayed(roundCreation.isPlayed());

        if (round.isPlayed() && !rounds.isEmpty()) {
            for (int i = 0; i < rounds.size(); i++) {
                rounds.get(i).setStartTime(round.getStartTime().plusMinutes(20 * i));
                rounds.get(i).setEndTime(round.getStartTime().plusMinutes(20 * i + 20));
                roundRepository.save(rounds.get(i));
            }
        }

        return roundRepository.save(round);
    }

    public Round updateRound(Long roundId, RoundDTO roundCreation) throws EntityNotFoundException{
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        round.setPlayed(roundCreation.isPlayed());
        return roundRepository.save(round);
    }

    public Game updateGame(Long gameId, GameDTO gameCreation) throws EntityNotFoundException{
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
        game.setSwitchGame(gameCreation.getSwitchGame());
        return gameRepository.save(game);
    }

    public Points updatePoints(Long roundId, Long teamId, Long gameId, PointsDTO pointsCreation) throws EntityNotFoundException{
        Points points = pointsRepository.findByGameIdAndTeamId(gameId, teamId).orElseThrow(() -> new EntityNotFoundException("There are no points with this ID."));
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

        if (round.isFinalGame()){
            points.setFinal_points(pointsCreation.getPoints());
        } else {
            points.setNormal_points(pointsCreation.getPoints());
        }

        points.setGame(game);
        points.setTeam(team);
        return pointsRepository.save(points);
    }

    public List<Round> createFinalPlan() {
        List<Team> teams = teamRepository.findByFinalReadyTrue();

        teams.sort((t1, t2) -> {
            int t1Points = t1.getPoints().stream().mapToInt(Points::getNormal_points).sum();
            int t2Points = t2.getPoints().stream().mapToInt(Points::getNormal_points).sum();
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
            point.setNormal_points(0);
            point.setFinal_points(3-i);
            point.setTeam(teams.get(i));
            point.setGame(game);
            pointsRepository.save(point);
        }

        for (int i = 0; i < 2; i++){
            createFinalRounds(teams, LocalDateTime.now().plusMinutes(20 * i));
        }

        return roundRepository.findByFinalGameTrue();
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
                point.setNormal_points(0);
                point.setFinal_points(0);
                point.setTeam(team);
                point.setGame(game);
                pointsRepository.save(point);
            }

        }
    }


}
