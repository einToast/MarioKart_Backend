package de.fsr.mariokart_backend.registration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.CharacterNames;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationCreateService;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AddCharacterServiceTest {

    @Mock
    private AdminRegistrationReadService adminRegistrationReadService;

    @Mock
    private AdminRegistrationCreateService adminRegistrationCreateService;

    @InjectMocks
    private AddCharacterService service;

    @Test
    void addCharactersThrowsWhenCharactersAlreadyExist() {
        when(adminRegistrationReadService.getCharacters()).thenReturn(List.of(new CharacterReturnDTO(1L, "Mario")));

        assertThatThrownBy(() -> service.addCharacters())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Characters already exist");

        verifyNoInteractions(adminRegistrationCreateService);
    }

    @Test
    void addCharactersCreatesEveryCharacterOfTheList() {
        when(adminRegistrationReadService.getCharacters())
                .thenReturn(List.of())
                .thenReturn(List.of(new CharacterReturnDTO(1L, "Mario"), new CharacterReturnDTO(2L, "Luigi")));

        when(adminRegistrationCreateService.addCharacters(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Character> result = service.addCharacters();

        assertThat(result).extracting(Character::getCharacterName)
                .containsExactlyElementsOf(CharacterNames.ALL);

        ArgumentCaptor<List<Character>> captor = ArgumentCaptor.captor();
        verify(adminRegistrationCreateService).addCharacters(captor.capture());
        assertThat(captor.getValue()).extracting(Character::getCharacterName)
                .containsExactlyElementsOf(CharacterNames.ALL);
    }

    @Test
    void addCharactersKeepsTheHyphensInTheCharacterNames() {
        when(adminRegistrationReadService.getCharacters()).thenReturn(List.of());

        List<Character> result = service.addCharacters();

        // The frontend loads the image of a character by its name
        assertThat(result).extracting(Character::getCharacterName).contains("Donkey-Kong", "Wendy-O.-Koopa");
    }

    @Test
    void characterNamesContainEveryCharacterOnce() {
        assertThat(CharacterNames.ALL).hasSize(41).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @CsvSource({
            "Baby-Daisy, Baby Daisy",
            "Baby-Luigi, Baby Luigi",
            "Baby-Mario, Baby Mario",
            "Baby-Peach, Baby Peach",
            "Baby-Rosalina, Baby Rosalina",
            "Donkey-Kong, Donkey Kong",
            "König-Buu-Huu, König Buu Huu",
            "Shy-Guy, Shy Guy"
    })
    void getCorrectedNameWritesNamesOfSeveralWordsWithSpaces(String imageName, String ingameName) {
        assertThat(service.getCorrectedName(imageName)).isEqualTo(ingameName);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Inkling-Junge", "Inkling-Mädchen", "Katzen-Peach", "Knochen-Bowser", "Metall-Mario",
            "Rosagold-Peach", "Tanuki-Mario" })
    void getCorrectedNameKeepsTheHyphenTheGameWrites(String imageName) {
        assertThat(service.getCorrectedName(imageName)).isEqualTo(imageName);
    }

    @ParameterizedTest
    @CsvSource({
            "Bowser-Jr, Bowser Jr.",
            "Iggy-Koopa, Iggy",
            "Larry-Koopa, Larry",
            "Lemmy-Koopa, Lemmy",
            "Ludwig-von-Koopa, Ludwig",
            "Morton-Koopa-Jr, Morton",
            "Roy-Koopa, Roy",
            "Wendy-O.-Koopa, Wendy"
    })
    void getCorrectedNameUsesTheNameTheGameShows(String imageName, String ingameName) {
        assertThat(service.getCorrectedName(imageName)).isEqualTo(ingameName);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Mario", "Bewohnerin", "Knochentrocken" })
    void getCorrectedNameKeepsNamesOfOneWord(String imageName) {
        assertThat(service.getCorrectedName(imageName)).isEqualTo(imageName);
    }

    @Test
    void getCorrectedNameKnowsEveryCharacter() {
        // A new character with a hyphen has to be checked against the name the game shows
        assertThat(CharacterNames.ALL)
                .filteredOn(characterName -> characterName.contains("-"))
                .containsExactlyInAnyOrder(
                        "Baby-Daisy", "Baby-Luigi", "Baby-Mario", "Baby-Peach", "Baby-Rosalina", "Donkey-Kong",
                        "König-Buu-Huu", "Shy-Guy",
                        "Inkling-Junge", "Inkling-Mädchen", "Katzen-Peach", "Knochen-Bowser", "Metall-Mario",
                        "Rosagold-Peach", "Tanuki-Mario",
                        "Bowser-Jr", "Iggy-Koopa", "Larry-Koopa", "Lemmy-Koopa", "Ludwig-von-Koopa",
                        "Morton-Koopa-Jr", "Roy-Koopa", "Wendy-O.-Koopa");
    }

}
