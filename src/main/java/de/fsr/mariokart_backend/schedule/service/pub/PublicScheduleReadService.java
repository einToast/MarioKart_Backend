package de.fsr.mariokart_backend.schedule.service.pub;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicScheduleReadService {

    private final RoundRepository roundRepository;
    private final ScheduleReturnDTOService scheduleReturnDTOService;

    public List<RoundReturnDTO> getCurrentRounds() {
        List<Round> rounds = roundRepository.findByPlayedFalse();
        rounds.sort(Comparator.comparing(Round::getStartTime));
        if (rounds.size() > 2) {
            rounds = rounds.subList(0, 2);
        }

        List<RoundReturnDTO> roundReturnDTOs = rounds.stream()
                .map(scheduleReturnDTOService::roundToRoundDTO)
                .toList();

        roundReturnDTOs.forEach(roundReturnDTO -> {
            roundReturnDTO.getGames().forEach(game -> {
                game.setTeams(deleteUnnecessaryInformationFromTeams(game.getTeams()));
                game.setPoints(null);
            });
        });

        return roundReturnDTOs;

    }

    public Integer getNumberOfRoundsUnplayed() {
        return roundRepository.countByPlayedFalse();
    }

    public Boolean isScheduleCreated() {
        return !roundRepository.findAll().isEmpty();
    }

    public Boolean isFinalScheduleCreated() {
        return !roundRepository.findByFinalGameTrue().isEmpty();
    }

    public Set<TeamReturnDTO> deleteUnnecessaryInformationFromTeams(Set<TeamReturnDTO> teams) {
        if (!isFinalScheduleCreated()) {
            teams.forEach(team -> team.setGroupPoints(0));
        }
        teams.forEach(team -> team.setFinalPoints(0));
        return teams;
    }
}