package de.fsr.mariokart_backend.registration.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
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
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminRegistrationUpdateServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @Mock
    private RegistrationReturnDTOService registrationReturnDTOService;

    @InjectMocks
    private AdminRegistrationUpdateService service;

    @Test
    void updateTeamThrowsWhenTeamDoesNotExist() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTeam(1L, new TeamInputDTO("Speedsters", "Mario", true, true)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("team with this ID");
    }

    @Test
    void updateTeamThrowsWhenNewTeamNameAlreadyExists() {
        Team current = team(1L, "Speedsters", character("Mario"));
        Team other = team(2L, "Other", character("Luigi"));
        TeamInputDTO update = new TeamInputDTO("Other", "Mario", true, true);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(current));
        when(teamRepository.existsByTeamName("Other")).thenReturn(true);
        when(teamRepository.findByTeamName("Other")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.updateTeam(1L, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team name already exists");
    }

    @Test
    void updateTeamThrowsWhenCharacterDoesNotExist() {
        Team current = team(1L, "Speedsters", character("Mario"));
        TeamInputDTO update = new TeamInputDTO("Speedsters", "Peach", true, true);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(current));
        when(characterRepository.findByCharacterName("Peach")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTeam(1L, update))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("character with this name");
    }

    @Test
    void updateTeamThrowsWhenCharacterAlreadyAssignedToDifferentTeam() {
        Team current = team(1L, "Speedsters", character("Mario"));
        Team occupied = team(2L, "Other", character("Luigi"));
        Character peach = character("Peach");
        peach.setTeam(occupied);
        TeamInputDTO update = new TeamInputDTO("Speedsters", "Peach", true, true);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(current));
        when(characterRepository.findByCharacterName("Peach")).thenReturn(Optional.of(peach));

        assertThatThrownBy(() -> service.updateTeam(1L, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Character is already in a team");
    }

    @Test
    void updateTeamUpdatesFieldsWhenFinalScheduleNotCreated() throws Exception {
        Team current = team(1L, "Speedsters", character("Mario"));
        Character peach = character("Peach");
        TeamInputDTO update = new TeamInputDTO("Turbo", "Peach", false, false);
        TeamReturnDTO expected = dto(1L, "Turbo", "Peach", false, false);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(current));
        when(teamRepository.existsByTeamName("Turbo")).thenReturn(false);
        when(characterRepository.findByCharacterName("Peach")).thenReturn(Optional.of(peach));
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);
        when(teamRepository.save(current)).thenReturn(current);
        when(registrationReturnDTOService.teamToTeamReturnDTO(current)).thenReturn(expected);

        TeamReturnDTO result = service.updateTeam(1L, update);

        assertThat(result).isEqualTo(expected);
        assertThat(current.getTeamName()).isEqualTo("Turbo");
        assertThat(current.getCharacter()).isEqualTo(peach);
        assertThat(current.isFinalReady()).isFalse();
        assertThat(current.isActive()).isFalse();
    }

    @Test
    void updateTeamDoesNotChangeFinalFlagsWhenFinalScheduleAlreadyCreated() throws Exception {
        Team current = team(1L, "Speedsters", character("Mario"));
        current.setFinalReady(true);
        current.setActive(true);
        TeamInputDTO update = new TeamInputDTO("Speedsters", "Mario", false, false);
        TeamReturnDTO expected = dto(1L, "Speedsters", "Mario", true, true);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(current));
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(true);
        when(teamRepository.save(current)).thenReturn(current);
        when(registrationReturnDTOService.teamToTeamReturnDTO(current)).thenReturn(expected);

        TeamReturnDTO result = service.updateTeam(1L, update);

        assertThat(result).isEqualTo(expected);
        assertThat(current.isFinalReady()).isTrue();
        assertThat(current.isActive()).isTrue();
    }

    @Test
    void resetEveryTeamFinalParticipationSetsAllTeamsToActiveAndFinalReady() {
        Team first = team(1L, "A", character("Mario"));
        first.setFinalReady(false);
        first.setActive(false);
        Team second = team(2L, "B", character("Luigi"));
        second.setFinalReady(false);
        second.setActive(true);

        TeamReturnDTO firstDto = dto(1L, "A", "Mario", true, true);
        TeamReturnDTO secondDto = dto(2L, "B", "Luigi", true, true);

        when(teamRepository.findAll()).thenReturn(List.of(first, second));
        when(teamRepository.saveAll(List.of(first, second))).thenReturn(List.of(first, second));
        when(registrationReturnDTOService.teamToTeamReturnDTO(first)).thenReturn(firstDto);
        when(registrationReturnDTOService.teamToTeamReturnDTO(second)).thenReturn(secondDto);

        List<TeamReturnDTO> result = service.resetEveryTeamFinalParticipation();

        assertThat(result).containsExactly(firstDto, secondDto);
        assertThat(first.isFinalReady()).isTrue();
        assertThat(first.isActive()).isTrue();
        assertThat(second.isFinalReady()).isTrue();
        assertThat(second.isActive()).isTrue();
    }

    private static Character character(String name) {
        Character character = new Character();
        character.setCharacterName(name);
        return character;
    }

    private static Team team(Long id, String name, Character character) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        team.setCharacter(character);
        team.setFinalReady(true);
        team.setActive(true);
        return team;
    }

    private static TeamReturnDTO dto(Long id, String teamName, String characterName, boolean finalReady, boolean active) {
        return new TeamReturnDTO(id, teamName, new CharacterReturnDTO(id, characterName), finalReady, active, 0, 0, 0);
    }
}
