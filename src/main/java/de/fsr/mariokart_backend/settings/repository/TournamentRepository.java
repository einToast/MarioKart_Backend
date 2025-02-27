package de.fsr.mariokart_backend.settings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fsr.mariokart_backend.settings.model.Tournament;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

}
