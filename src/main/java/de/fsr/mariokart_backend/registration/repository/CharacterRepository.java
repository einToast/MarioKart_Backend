package de.fsr.mariokart_backend.registration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.registration.model.Character;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByCharacterName(String characterName);

    List<Character> findByTeamIsNull();

    List<Character> findByTeamIsNotNull();
}
