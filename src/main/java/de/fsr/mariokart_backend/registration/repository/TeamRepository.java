package de.fsr.mariokart_backend.registration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.registration.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);

    List<Team> findByFinalReadyTrue();

    Optional<Team> findByTeamName(String teamName);

    List<Team> findAllByOrderByTeamNameAsc();
}