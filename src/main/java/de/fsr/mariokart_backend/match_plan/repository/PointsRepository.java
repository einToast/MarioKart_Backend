package de.fsr.mariokart_backend.match_plan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.match_plan.model.Points;

import java.util.Optional;

@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {
    Optional<Points> findByGameIdAndTeamId(Long gameId, Long teamId);
}
