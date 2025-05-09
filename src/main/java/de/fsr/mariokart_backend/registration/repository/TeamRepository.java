package de.fsr.mariokart_backend.registration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.fsr.mariokart_backend.registration.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);

    // boolean existsByCharacterName(String characterName);
    List<Team> findByFinalReadyTrue();

    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.groupPoints) DESC")
    List<Team> findAllByOrderByGroupPointsDesc();

    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.finalPoints) DESC")
    List<Team> findAllByOrderByFinalPointsDesc();

    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.finalPoints) DESC, SUM(p.groupPoints) DESC")
    List<Team> findAllByOrderByFinalPointsDescGroupPointsDesc();

    Optional<Team> findByTeamName(String teamName);

    List<Team> findAllByOrderByTeamNameAsc();
}