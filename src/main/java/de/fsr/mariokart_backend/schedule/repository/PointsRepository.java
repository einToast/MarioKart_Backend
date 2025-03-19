package de.fsr.mariokart_backend.schedule.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fsr.mariokart_backend.schedule.model.Points;

@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {
    Optional<Points> findByGameIdAndTeamId(Long gameId, Long teamId);
}
