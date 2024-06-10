package de.fsr.mariokart_backend.registration.service;

import de.fsr.mariokart_backend.registration.model.Character;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AddCharacterService {

    private final RegistrationService registrationService;

    public List<Character> addCharacters(String directoryPath) throws IOException, IllegalStateException {
        List<String> imageNames = getImageNames(directoryPath);
        List<Character> characters = new ArrayList<>();

        if (!registrationService.getCharacters().isEmpty()) {
            throw new IllegalStateException("Characters already exist.");
        }

        for (String imageName : imageNames) {
            Character character = new Character();
            character.setCharacterName(imageName);
            characters.add(character);
        }

        registrationService.addCharacters(characters);

        return characters;



    }

    public List<String> getImageNames(String directoryPath) throws IOException{
        List<String> imageNames = new ArrayList<>();
        DirectoryStream.Filter<Path> filter = entry -> {
            String fileName = entry.getFileName().toString().toLowerCase();
            return fileName.endsWith(".png");
        };

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directoryPath), filter)) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                imageNames.add(nameWithoutExtension);
            }
        }

        System.out.println(imageNames);

        return imageNames;

    }
}
