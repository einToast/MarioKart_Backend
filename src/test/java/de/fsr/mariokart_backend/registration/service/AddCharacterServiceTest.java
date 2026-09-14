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

        ArgumentCaptor<List<Character>> captor = ArgumentCaptor.forClass(List.class);
        verify(adminRegistrationCreateService).addCharacters(captor.capture());
        assertThat(captor.getValue()).extracting(Character::getCharacterName).containsExactly("Mario", "Luigi");
    }

    @Test
    void getImageNamesThrowsWhenDirectoryDoesNotExist() {
        assertThatThrownBy(() -> service.getImageNames("missing-directory"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("does not exist");
    }
}
