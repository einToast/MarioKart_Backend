package de.fsr.mariokart_backend.registration.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class RegistrationInputDTOServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private RegistrationInputDTOService service;

    @Test
    void teamInputDTOToTeamMapsAllFields() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", false, false);
        Character mario = new Character(1L, "Mario", null);

        when(characterRepository.findByCharacterName("Mario")).thenReturn(Optional.of(mario));

        Team result = service.teamInputDTOToTeam(input);

        assertThat(result.getTeamName()).isEqualTo("Speedsters");
        assertThat(result.getCharacter()).isEqualTo(mario);
        assertThat(result.isFinalReady()).isFalse();
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void teamInputDTOToTeamThrowsWhenCharacterDoesNotExist() {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Unknown", true, true);

        when(characterRepository.findByCharacterName("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.teamInputDTOToTeam(input))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("character with this name");
    }
}
