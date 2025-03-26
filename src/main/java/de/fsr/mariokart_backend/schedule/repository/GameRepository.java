package de.fsr.mariokart_backend.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fsr.mariokart_backend.schedule.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByRoundId(Long id);
}