package de.fsr.mariokart_backend.registration.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminRegistrationCreateServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private AdminRegistrationCreateService service;

    @Test
    void addCharacterDelegatesToRepository() {
        Character character = new Character();
        character.setCharacterName("Mario");
        when(characterRepository.save(character)).thenReturn(character);

        assertThat(service.addCharacter(character)).isSameAs(character);
    }

    @Test
    void addCharactersDelegatesToRepository() {
        Character mario = new Character();
        mario.setCharacterName("Mario");
        Character luigi = new Character();
        luigi.setCharacterName("Luigi");
        List<Character> characters = List.of(mario, luigi);

        when(characterRepository.saveAll(characters)).thenReturn(characters);

        assertThat(service.addCharacters(characters)).containsExactly(mario, luigi);
    }
}
