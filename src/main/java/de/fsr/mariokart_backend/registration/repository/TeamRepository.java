package de.fsr.mariokart_backend.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.registration.model.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);
//    boolean existsByCharacterName(String characterName);
    List<Team> findByFinalReadyTrue();

    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.groupPoints) DESC")
    List<Team> findAllByOrderByGroupPointsDesc();
    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.finalPoints) DESC")
    List<Team> findAllByOrderByFinalPointsDesc();

    @Query("SELECT t FROM Team t JOIN t.points p GROUP BY t.id ORDER BY SUM(p.finalPoints) DESC, SUM(p.groupPoints) DESC")
    List<Team> findAllByOrderByFinalPointsDescGroupPointsDesc();

    Optional<Team> findByTeamName(String teamName);

//    @Query("DELETE FROM Team t WHERE t.id = :id")
//    @Modifying
//    @Transactional
//    void deleteById(@Param("id") Long id);

}