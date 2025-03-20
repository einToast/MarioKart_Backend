package de.fsr.mariokart_backend.registration.service.admin;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Character;
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

        if (team.getCharacter() != null) {
            Character character = team.getCharacter();
            team.removeCharacter();
            character.removeTeam();
        }

        teamRepository.deleteById(id);
    }

    public void deleteAllTeams() throws RoundsAlreadyExistsException {
        if (publicScheduleReadService.isMatchPlanCreated()) {
            throw new RoundsAlreadyExistsException("Match schedule already exists");
        }

        List<Team> teams = teamRepository.findAll();
        for (Team team : teams) {
            if (team.getCharacter() != null) {
                Character character = team.getCharacter();
                team.removeCharacter();
                character.removeTeam();
            }
            if (team.getPoints() != null) {
                team.setPoints(Set.of());
            }
            teamRepository.delete(team);
        }
    }
}