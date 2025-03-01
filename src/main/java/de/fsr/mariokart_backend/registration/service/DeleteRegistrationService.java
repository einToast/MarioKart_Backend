package de.fsr.mariokart_backend.registration.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DeleteRegistrationService {

    private final TeamRepository teamRepository;
    private final RoundRepository roundRepository;

    public void deleteAllTeams() throws RoundsAlreadyExistsException {

        if (!roundRepository.findAll().isEmpty()) {
            throw new RoundsAlreadyExistsException("Match plan already exists");
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
