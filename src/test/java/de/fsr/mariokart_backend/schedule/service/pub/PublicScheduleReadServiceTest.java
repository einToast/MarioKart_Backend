package de.fsr.mariokart_backend.schedule.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicScheduleReadServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private ScheduleReturnDTOService scheduleReturnDTOService;

    @InjectMocks
    private PublicScheduleReadService service;

    @Test
    void getCurrentRoundsSortsLimitsAndRemovesPointDetails() {
        LocalDateTime now = LocalDateTime.now();
        Round late = buildRound(3L, now.plusMinutes(20));
        Round early = buildRound(1L, now.minusMinutes(10));
        Round middle = buildRound(2L, now.plusMinutes(5));

        when(roundRepository.findByPlayedFalse()).thenReturn(new java.util.ArrayList<>(List.of(late, early, middle)));
        when(roundRepository.findByFinalGameTrue()).thenReturn(List.of());
        when(scheduleReturnDTOService.roundToRoundDTO(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            TeamReturnDTO team = new TeamReturnDTO(1L, "Alpha", null, true, true, 12, 8, 2);
            GameReturnDTO game = new GameReturnDTO(100L + round.getId(), "Blue", new HashSet<>(Set.of(team)),
                    new HashSet<>());
            return new RoundReturnDTO(round.getId(), round.getRoundNumber(), round.getStartTime(), round.getEndTime(),
                    false, false, new HashSet<>(Set.of(game)), null);
        });

        List<RoundReturnDTO> result = service.getCurrentRounds();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        result.forEach(round -> round.getGames().forEach(game -> {
            assertThat(game.getPoints()).isNull();
            game.getTeams().forEach(team -> {
                assertThat(team.getGroupPoints()).isZero();
                assertThat(team.getFinalPoints()).isZero();
            });
        }));
    }

    @Test
    void getNumberOfRoundsUnplayedDelegatesToRepository() {
        when(roundRepository.countByPlayedFalse()).thenReturn(4);

        assertThat(service.getNumberOfRoundsUnplayed()).isEqualTo(4);
    }

    @Test
    void isScheduleCreatedReflectsWhetherRoundsExist() {
        when(roundRepository.findAll()).thenReturn(List.of(new Round()));

        assertThat(service.isScheduleCreated()).isTrue();
    }

    @Test
    void isFinalScheduleCreatedReflectsFinalRounds() {
        when(roundRepository.findByFinalGameTrue()).thenReturn(List.of(new Round()));

        assertThat(service.isFinalScheduleCreated()).isTrue();
    }

    @Test
    void deleteUnnecessaryInformationFromTeamsKeepsGroupPointsWhenFinalExists() {
        TeamReturnDTO team = new TeamReturnDTO(1L, "Alpha", null, true, true, 9, 6, 2);
        Set<TeamReturnDTO> teams = new HashSet<>(Set.of(team));

        when(roundRepository.findByFinalGameTrue()).thenReturn(List.of(new Round()));

        Set<TeamReturnDTO> sanitized = service.deleteUnnecessaryInformationFromTeams(teams);

        TeamReturnDTO first = sanitized.iterator().next();
        assertThat(first.getGroupPoints()).isEqualTo(9);
        assertThat(first.getFinalPoints()).isZero();
    }

    private Round buildRound(Long id, LocalDateTime startTime) {
        Round round = new Round();
        round.setId(id);
        round.setRoundNumber(id.intValue());
        round.setStartTime(startTime);
        round.setEndTime(startTime.plusMinutes(20));
        round.setPlayed(false);
        round.setFinalGame(false);
        return round;
    }
}
