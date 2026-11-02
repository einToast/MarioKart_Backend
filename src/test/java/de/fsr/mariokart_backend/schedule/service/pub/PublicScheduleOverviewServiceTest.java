package de.fsr.mariokart_backend.schedule.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationReadService;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.ScheduleOverviewDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicScheduleOverviewServiceTest {

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @Mock
    private PublicRegistrationReadService publicRegistrationReadService;

    @Mock
    private PublicSettingsReadService publicSettingsReadService;

    @InjectMocks
    private PublicScheduleOverviewService service;

    @Test
    void getOverviewCombinesRoundsPausingTeamsAndFlags() {
        RoundReturnDTO current = new RoundReturnDTO(5L, 5, null, null, false, false, Set.of(), null);
        RoundReturnDTO next = new RoundReturnDTO(6L, 6, null, null, false, false, Set.of(), null);
        TeamReturnDTO resting = new TeamReturnDTO(9L, "Team Yoshi", null, true, true, 0, 0, 2);
        TeamReturnDTO waiting = new TeamReturnDTO(8L, "Team Wario", null, true, true, 0, 0, 2);

        when(publicScheduleReadService.getCurrentRounds()).thenReturn(List.of(current, next));
        when(publicRegistrationReadService.getTeamsNotInRound(5L)).thenReturn(List.of(resting));
        when(publicRegistrationReadService.getTeamsNotInRound(6L)).thenReturn(List.of(waiting));
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);
        when(publicScheduleReadService.getNumberOfRoundsUnplayed()).thenReturn(4);
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, false, 4, null));

        ScheduleOverviewDTO overview = service.getOverview();

        assertThat(overview.getCurrentRounds()).containsExactly(current, next);
        assertThat(overview.getTeamsNotInRound())
                .containsEntry(5L, List.of(resting))
                .containsEntry(6L, List.of(waiting));
        assertThat(overview.isScheduleCreated()).isTrue();
        assertThat(overview.isFinalScheduleCreated()).isFalse();
        assertThat(overview.getNumberOfRoundsUnplayed()).isEqualTo(4);
        assertThat(overview.isTournamentOpen()).isTrue();
    }

    @Test
    void getOverviewTreatsMissingTournamentFlagAsClosed() {
        when(publicScheduleReadService.getCurrentRounds()).thenReturn(List.of());
        when(publicScheduleReadService.getNumberOfRoundsUnplayed()).thenReturn(0);
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(null, false, 4, null));

        ScheduleOverviewDTO overview = service.getOverview();

        assertThat(overview.getCurrentRounds()).isEmpty();
        assertThat(overview.getTeamsNotInRound()).isEmpty();
        assertThat(overview.isTournamentOpen()).isFalse();
    }
}
