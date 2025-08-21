package de.fsr.mariokart_backend.registration.service.admin;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "registration")
@CacheEvict(allEntries = true)
public class AdminRegistrationCreateService {
    private final CharacterRepository characterRepository;

    public Character addCharacter(Character character) {
        return characterRepository.save(character);
    }

    public List<Character> addCharacters(List<Character> characters) {
        return characterRepository.saveAll(characters);
    }
}