package de.fsr.mariokart_backend.registration.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AddCharacterService {

    private final RegistrationReadService registrationReadService;
    private final RegistrationCreateService registrationCreateService;

    public List<Character> addCharacters(String directoryPath) throws IOException, IllegalStateException {
        List<String> imageNames = getImageNames(directoryPath);
        List<Character> characters = new ArrayList<>();

        if (!registrationReadService.getCharacters().isEmpty()) {
            throw new IllegalStateException("Characters already exist.");
        }

        for (String imageName : imageNames) {
            Character character = new Character();
            character.setCharacterName(imageName);
            characters.add(character);
        }

        registrationCreateService.addCharacters(characters);

        System.out.println(registrationReadService.getCharacters()
                .stream()
                .map(CharacterReturnDTO::getCharacterName)
                .collect(Collectors.toList()));

        return characters;

    }

    public List<String> getImageNames(String directoryPath) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Suchmuster für Dateien im Ordner "static/media"
        Resource[] resources = resolver.getResources("classpath:static/" + directoryPath + "/*.png");

        return Stream.of(resources)
                .map(Resource::getFilename).filter(Objects::nonNull)
                .map(filename -> filename.replaceFirst("\\.png$", ""))
                .collect(Collectors.toList());
    }
}
