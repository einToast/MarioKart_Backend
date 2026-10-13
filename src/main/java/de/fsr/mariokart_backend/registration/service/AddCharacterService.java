package de.fsr.mariokart_backend.registration.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationCreateService;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AddCharacterService {

    // Image names that the game itself writes with a hyphen.
    private static final Set<String> HYPHENATED_INGAME_NAMES = Set.of(
            "Inkling-Junge", "Inkling-Mädchen", "Katzen-Peach", "Knochen-Bowser", "Metall-Mario", "Rosagold-Peach",
            "Tanuki-Mario");

    // Image names that differ from the in-game name by more than the hyphen.
    private static final Map<String, String> DIFFERENT_INGAME_NAMES = Map.of(
            "Bowser-Jr", "Bowser Jr.",
            "Iggy-Koopa", "Iggy",
            "Larry-Koopa", "Larry",
            "Lemmy-Koopa", "Lemmy",
            "Ludwig-von-Koopa", "Ludwig",
            "Morton-Koopa-Jr", "Morton",
            "Roy-Koopa", "Roy",
            "Wendy-O.-Koopa", "Wendy");

    private final AdminRegistrationReadService adminRegistrationReadService;
    private final AdminRegistrationCreateService adminRegistrationCreateService;

    public List<Character> addCharacters(String directoryPath) throws IOException, IllegalStateException {
        List<String> imageNames = getImageNames(directoryPath);
        List<Character> characters = new ArrayList<>();

        if (!adminRegistrationReadService.getCharacters().isEmpty()) {
            throw new IllegalStateException("Characters already exist.");
        }

        for (String imageName : imageNames) {
            Character character = new Character();
            character.setCharacterName(imageName);
            characters.add(character);
        }

        adminRegistrationCreateService.addCharacters(characters);

        log.info("Added characters: {}", adminRegistrationReadService.getCharacters()
                .stream()
                .map(CharacterReturnDTO::getCharacterName)
                .toList());

        return characters;

    }

    public List<String> getImageNames(String directoryPath) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Match PNG files in the "static/media" directory.
        Resource[] resources = resolver.getResources("classpath:static/" + directoryPath + "/*.png");

        return Stream.of(resources)
                .map(Resource::getFilename).filter(Objects::nonNull)
                .map(filename -> filename.replaceFirst("\\.png$", ""))
                .toList();
    }

    public String getCorrectedName(String name) {
        if (DIFFERENT_INGAME_NAMES.containsKey(name)) {
            return DIFFERENT_INGAME_NAMES.get(name);
        }
        if (HYPHENATED_INGAME_NAMES.contains(name)) {
            return name;
        }
        return name.replace("-", " ");
    }
}
