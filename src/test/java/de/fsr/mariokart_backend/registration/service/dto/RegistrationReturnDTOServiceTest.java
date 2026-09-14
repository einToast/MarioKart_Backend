package de.fsr.mariokart_backend.registration.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class RegistrationReturnDTOServiceTest {

    @Mock
    private PublicSettingsReadService publicSettingsReadService;

    @InjectMocks
    private RegistrationReturnDTOService service;

    @Test
    void characterToCharacterReturnDTOReturnsNullWhenInputNull() {
        assertThat(service.characterToCharacterReturnDTO(null)).isNull();
    }

    @Test
    void characterToCharacterReturnDTOMapsCharacter() {
        Character mario = new Character(1L, "Mario", null);

        CharacterReturnDTO result = service.characterToCharacterReturnDTO(mario);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCharacterName()).isEqualTo("Mario");
    }

    @Test
    void teamToTeamReturnDTOReturnsNullWhenInputNull() {
        assertThat(service.teamToTeamReturnDTO(null)).isNull();
    }

    @Test
    void teamToTeamReturnDTOMapsTeamUsingConfiguredMaxGames() {
        Team team = mock(Team.class);
        Character peach = new Character(2L, "Peach", null);

        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, true, 6));
        when(team.getId()).thenReturn(1L);
        when(team.getTeamName()).thenReturn("Turbo");
        when(team.getCharacter()).thenReturn(peach);
        when(team.isFinalReady()).thenReturn(false);
        when(team.isActive()).thenReturn(true);
        when(team.getGroupPoints(6)).thenReturn(14);
        when(team.getFinalPoints()).thenReturn(9);
        when(team.getNumberOfGamesPlayed(6)).thenReturn(4);

        TeamReturnDTO result = service.teamToTeamReturnDTO(team);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTeamName()).isEqualTo("Turbo");
        assertThat(result.getCharacter().getCharacterName()).isEqualTo("Peach");
        assertThat(result.isFinalReady()).isFalse();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getGroupPoints()).isEqualTo(14);
        assertThat(result.getFinalPoints()).isEqualTo(9);
        assertThat(result.getNumberOfGamesPlayed()).isEqualTo(4);
    }
}
