package de.fsr.mariokart_backend.registration.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminRegistrationReadServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private RegistrationReturnDTOService registrationReturnDTOService;

    @Mock
    private PublicSettingsReadService publicSettingsReadService;

    @InjectMocks
    private AdminRegistrationReadService service;

    @Test
    void getTeamsMapsRepositoryTeams() {
        Team first = mock(Team.class);
        Team second = mock(Team.class);
        TeamReturnDTO firstDto = teamDto(1L, "A");
        TeamReturnDTO secondDto = teamDto(2L, "B");

        when(teamRepository.findAll()).thenReturn(List.of(first, second));
        when(registrationReturnDTOService.teamToTeamReturnDTO(first)).thenReturn(firstDto);
        when(registrationReturnDTOService.teamToTeamReturnDTO(second)).thenReturn(secondDto);

        List<TeamReturnDTO> result = service.getTeams();

        assertThat(result).containsExactly(firstDto, secondDto);
    }

    @Test
    void getTeamsSortedByGroupPointsSortsByAverageDescending() {
        Team highAverage = mock(Team.class);
        Team lowAverage = mock(Team.class);

        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, true, 4));
        when(teamRepository.findAll()).thenReturn(List.of(lowAverage, highAverage));

        when(lowAverage.getGroupPoints(4)).thenReturn(8);
        when(lowAverage.getNumberOfGamesPlayed(4)).thenReturn(2);

        when(highAverage.getGroupPoints(4)).thenReturn(15);
        when(highAverage.getNumberOfGamesPlayed(4)).thenReturn(3);

        List<Team> result = service.getTeamsSortedByGroupPoints();

        assertThat(result).containsExactly(highAverage, lowAverage);
    }

    @Test
    void getTeamsReturnDTOSortedByGroupPointsFallsBackToGetTeamsWhenEmpty() {
        AdminRegistrationReadService spyService = spy(service);
        List<TeamReturnDTO> fallback = List.of(teamDto(1L, "Fallback"));

        doReturn(List.of()).when(spyService).getTeamsSortedByGroupPoints();
        doReturn(fallback).when(spyService).getTeams();

        List<TeamReturnDTO> result = spyService.getTeamsReturnDTOSortedByGroupPoints();

        assertThat(result).isEqualTo(fallback);
        verify(spyService).getTeams();
    }

    @Test
    void getTeamsSortedByFinalPointsSortsDescending() {
        Team first = mock(Team.class);
        Team second = mock(Team.class);

        AdminRegistrationReadService spyService = spy(service);
        doReturn(List.of(first, second)).when(spyService).getTeamsSortedByGroupPoints();

        when(first.getFinalPoints()).thenReturn(5);
        when(second.getFinalPoints()).thenReturn(12);
        when(registrationReturnDTOService.teamToTeamReturnDTO(second)).thenReturn(teamDto(2L, "Second"));
        when(registrationReturnDTOService.teamToTeamReturnDTO(first)).thenReturn(teamDto(1L, "First"));

        List<TeamReturnDTO> result = spyService.getTeamsSortedByFinalPoints();

        assertThat(result).extracting(TeamReturnDTO::getTeamName).containsExactly("Second", "First");
    }

    @Test
    void getTeamsSortedByFinalPointsFallsBackToGetTeamsWhenEmpty() {
        AdminRegistrationReadService spyService = spy(service);
        List<TeamReturnDTO> fallback = List.of(teamDto(3L, "Fallback"));

        doReturn(List.of()).when(spyService).getTeamsSortedByGroupPoints();
        doReturn(fallback).when(spyService).getTeams();

        List<TeamReturnDTO> result = spyService.getTeamsSortedByFinalPoints();

        assertThat(result).isEqualTo(fallback);
        verify(spyService).getTeams();
    }

    @Test
    void getFinalTeamsReturnsOnlyFinalReadyAndAtMostFour() {
        Team t1 = mock(Team.class);
        Team t2 = mock(Team.class);
        Team t3 = mock(Team.class);
        Team t4 = mock(Team.class);
        Team t5 = mock(Team.class);

        when(t1.isFinalReady()).thenReturn(true);
        when(t2.isFinalReady()).thenReturn(false);
        when(t3.isFinalReady()).thenReturn(true);
        when(t4.isFinalReady()).thenReturn(true);
        when(t5.isFinalReady()).thenReturn(true);

        AdminRegistrationReadService spyService = spy(service);
        doReturn(List.of(t1, t2, t3, t4, t5)).when(spyService).getTeamsSortedByGroupPoints();

        List<Team> result = spyService.getFinalTeams();

        assertThat(result).containsExactly(t1, t3, t4, t5);
    }

    @Test
    void getFinalTeamsReturnDTOMapsFinalTeams() {
        Team t1 = mock(Team.class);
        Team t2 = mock(Team.class);
        TeamReturnDTO dto1 = teamDto(1L, "A");
        TeamReturnDTO dto2 = teamDto(2L, "B");

        AdminRegistrationReadService spyService = spy(service);
        doReturn(List.of(t1, t2)).when(spyService).getFinalTeams();
        when(registrationReturnDTOService.teamToTeamReturnDTO(t1)).thenReturn(dto1);
        when(registrationReturnDTOService.teamToTeamReturnDTO(t2)).thenReturn(dto2);

        List<TeamReturnDTO> result = spyService.getFinalTeamsReturnDTO();

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    void getCharactersMapsRepositoryCharacters() {
        Character mario = new Character(1L, "Mario", null);
        Character luigi = new Character(2L, "Luigi", null);
        CharacterReturnDTO marioDto = new CharacterReturnDTO(1L, "Mario");
        CharacterReturnDTO luigiDto = new CharacterReturnDTO(2L, "Luigi");

        when(characterRepository.findAll()).thenReturn(List.of(mario, luigi));
        when(registrationReturnDTOService.characterToCharacterReturnDTO(mario)).thenReturn(marioDto);
        when(registrationReturnDTOService.characterToCharacterReturnDTO(luigi)).thenReturn(luigiDto);

        List<CharacterReturnDTO> result = service.getCharacters();

        assertThat(result).containsExactly(marioDto, luigiDto);
    }

    private static TeamReturnDTO teamDto(Long id, String name) {
        return new TeamReturnDTO(id, name, new CharacterReturnDTO(id, "Mario"), true, true, 0, 0, 0);
    }
}
