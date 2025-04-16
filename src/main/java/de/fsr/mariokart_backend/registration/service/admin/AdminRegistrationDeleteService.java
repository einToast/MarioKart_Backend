package de.fsr.mariokart_backend.registration.service.admin;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminRegistrationDeleteService {
    private final TeamRepository teamRepository;
    private final PublicScheduleReadService publicScheduleReadService;

    public void deleteTeam(Long id) throws RoundsAlreadyExistsException, EntityNotFoundException {
        if (publicScheduleReadService.isMatchPlanCreated()) {
            throw new RoundsAlreadyExistsException("Match schedule already exists");
        }
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

        team.removeTeamAssociations();

        teamRepository.deleteById(id);
    }

    public void deleteAllTeams() throws RoundsAlreadyExistsException {
        if (publicScheduleReadService.isMatchPlanCreated()) {
            throw new RoundsAlreadyExistsException("Match schedule already exists");
        }

        List<Team> teams = teamRepository.findAll();
        for (Team team : teams) {
            if (team.getPoints() != null) {
                team.setPoints(Set.of());
            }
            team.removeTeamAssociations();
            teamRepository.delete(team);
        }
    }
}