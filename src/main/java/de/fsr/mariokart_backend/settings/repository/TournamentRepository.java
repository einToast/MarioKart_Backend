package de.fsr.mariokart_backend.settings.repository;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.settings.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

}
