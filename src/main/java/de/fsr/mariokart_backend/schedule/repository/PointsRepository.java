package de.fsr.mariokart_backend.schedule.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.fsr.mariokart_backend.schedule.model.Points;

public interface PointsRepository extends JpaRepository<Points, Long> {
    Optional<Points> findByGameIdAndTeamId(Long gameId, Long teamId);

    List<Points> findByGameId(Long id);

    @Query("select count(p) from Points p where p.game.round.finalGame = false group by p.game.id")
    List<Long> countTeamsPerGroupGame();

    // Points of this many teams per game count as they are, see FieldPointsMultiplier
    default int getSmallestGroupGameTeamCount() {
        return countTeamsPerGroupGame().stream().mapToInt(Long::intValue).min().orElse(0);
    }

}
