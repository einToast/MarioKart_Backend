package de.fsr.mariokart_backend.registration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
    void addCharactersThrowsWhenCharactersAlreadyExist() throws Exception {
        AddCharacterService spyService = spy(service);
        doReturn(List.of("Mario")).when(spyService).getImageNames("media/characters");
        when(adminRegistrationReadService.getCharacters()).thenReturn(List.of(new CharacterReturnDTO(1L, "Mario")));

        assertThatThrownBy(() -> spyService.addCharacters("media/characters"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Characters already exist");

        verifyNoInteractions(adminRegistrationCreateService);
    }

    @Test
    void addCharactersImportsAllImagesAndReturnsCreatedCharacters() throws Exception {
        AddCharacterService spyService = spy(service);
        doReturn(List.of("Mario", "Luigi")).when(spyService).getImageNames("media/characters");

        when(adminRegistrationReadService.getCharacters())
                .thenReturn(List.of())
                .thenReturn(List.of(new CharacterReturnDTO(1L, "Mario"), new CharacterReturnDTO(2L, "Luigi")));

        when(adminRegistrationCreateService.addCharacters(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Character> result = spyService.addCharacters("media/characters");

        assertThat(result).extracting(Character::getCharacterName).containsExactly("Mario", "Luigi");

        ArgumentCaptor<List<Character>> captor = ArgumentCaptor.captor();
        verify(adminRegistrationCreateService).addCharacters(captor.capture());
        assertThat(captor.getValue()).extracting(Character::getCharacterName).containsExactly("Mario", "Luigi");
    }

    @Test
    void addCharactersKeepsTheImageNamesAsCharacterNames() throws Exception {
        AddCharacterService spyService = spy(service);
        doReturn(List.of("Donkey-Kong", "Wendy-O.-Koopa")).when(spyService).getImageNames("media/characters");
        when(adminRegistrationReadService.getCharacters()).thenReturn(List.of());

        List<Character> result = spyService.addCharacters("media/characters");

        // The frontend loads the image of a character by its name
        assertThat(result).extracting(Character::getCharacterName).containsExactly("Donkey-Kong", "Wendy-O.-Koopa");
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
    void getCorrectedNameKnowsEveryCharacterImage() throws Exception {
        // A new image with a hyphen has to be checked against the name the game shows
        assertThat(service.getImageNames("media"))
                .filteredOn(imageName -> imageName.contains("-"))
                .containsExactlyInAnyOrder(
                        "Baby-Daisy", "Baby-Luigi", "Baby-Mario", "Baby-Peach", "Baby-Rosalina", "Donkey-Kong",
                        "König-Buu-Huu", "Shy-Guy",
                        "Inkling-Junge", "Inkling-Mädchen", "Katzen-Peach", "Knochen-Bowser", "Metall-Mario",
                        "Rosagold-Peach", "Tanuki-Mario",
                        "Bowser-Jr", "Iggy-Koopa", "Larry-Koopa", "Lemmy-Koopa", "Ludwig-von-Koopa",
                        "Morton-Koopa-Jr", "Roy-Koopa", "Wendy-O.-Koopa");
    }

    @Test
    void getImageNamesThrowsWhenDirectoryDoesNotExist() {
        assertThatThrownBy(() -> service.getImageNames("missing-directory"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("does not exist");
    }
}
