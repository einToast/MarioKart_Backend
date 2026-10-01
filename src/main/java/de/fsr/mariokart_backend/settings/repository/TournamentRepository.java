package de.fsr.mariokart_backend.settings.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.settings.model.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

}
