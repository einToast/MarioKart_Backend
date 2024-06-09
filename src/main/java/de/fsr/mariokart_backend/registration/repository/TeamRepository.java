package de.fsr.mariokart_backend.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.registration.model.Team;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);
    boolean existsByCharacterName(String characterName);
    List<Team> findByFinalReadyTrue();

    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.normal_points) DESC")
    List<Team> findAllByOrderByNormalPointsDesc();
    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.final_points) DESC")
    List<Team> findAllByOrderByFinalPointsDesc();
}