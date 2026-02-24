package de.fsr.mariokart_backend.registration.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminRegistrationDeleteServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @InjectMocks
    private AdminRegistrationDeleteService service;

    @Test
    void deleteTeamThrowsWhenScheduleExists() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.deleteTeam(1L))
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Schedule already exists");
    }

    @Test
    void deleteTeamThrowsWhenTeamMissing() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteTeam(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("team with this ID");
    }

    @Test
    void deleteTeamDeletesWhenAllowed() throws Exception {
        Team team = new Team();
        team.setId(1L);

        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        service.deleteTeam(1L);

        verify(teamRepository).deleteById(1L);
    }

    @Test
    void deleteAllTeamsThrowsWhenScheduleExists() {
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.deleteAllTeams())
                .isInstanceOf(RoundsAlreadyExistsException.class);
    }

    @Test
    void deleteAllTeamsClearsPointsAndDeletesTeamsWhenAllowed() throws Exception {
        Team first = new Team();
        first.setId(1L);
        Points points = new Points();
        first.setPoints(Set.of(points));

        Team second = new Team();
        second.setId(2L);

        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(teamRepository.findAll()).thenReturn(List.of(first, second));

        service.deleteAllTeams();

        assertThat(first.getPoints()).isEmpty();
        verify(teamRepository).delete(first);
        verify(teamRepository).delete(second);
    }
}
