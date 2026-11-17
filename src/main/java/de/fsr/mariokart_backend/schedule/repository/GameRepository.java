package de.fsr.mariokart_backend.schedule.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.schedule.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByRoundId(Long id);

    Optional<Game> findFirstByRound_FinalGameFalseOrderBySwitchIndexDesc();
}