package de.fsr.mariokart_backend.registration.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.CharacterNames;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationCreateService;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AddCharacterService {

    private final AdminRegistrationReadService adminRegistrationReadService;
    private final AdminRegistrationCreateService adminRegistrationCreateService;

    public List<Character> addCharacters() throws IllegalStateException {
        List<Character> characters = new ArrayList<>();

        if (!adminRegistrationReadService.getCharacters().isEmpty()) {
            throw new IllegalStateException("Characters already exist.");
        }

        for (String characterName : CharacterNames.ALL) {
            Character character = new Character();
            character.setCharacterName(characterName);
            characters.add(character);
        }

        adminRegistrationCreateService.addCharacters(characters);

        log.info("Added characters: {}", adminRegistrationReadService.getCharacters()
                .stream()
                .map(CharacterReturnDTO::getCharacterName)
                .toList());

        return characters;

    }

    public String getCorrectedName(String name) {
        if (CharacterNames.DIFFERENT_INGAME_NAMES.containsKey(name)) {
            return CharacterNames.DIFFERENT_INGAME_NAMES.get(name);
        }
        if (CharacterNames.HYPHENATED_INGAME_NAMES.contains(name)) {
            return name;
        }
        return name.replace("-", " ");
    }
}
