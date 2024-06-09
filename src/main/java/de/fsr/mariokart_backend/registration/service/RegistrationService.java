package de.fsr.mariokart_backend.registration.service;

import de.fsr.mariokart_backend.registration.model.dto.TeamDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;

@Service
@AllArgsConstructor
public class RegistrationService {

        private final TeamRepository teamRepository;

        public List<Team> getTeams() {
            return teamRepository.findAll();
        }

        public List<Team> getTeamsSortedByNormalPoints() {
            return teamRepository.findAllByOrderByNormalPointsDesc();
        }

        public List<Team> getTeamsSortedByFinalPoints() {
            return teamRepository.findAllByOrderByFinalPointsDesc();
        }

        public Team getTeamById(Long id) throws EntityNotFoundException{
            return teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));
        }

        public Team addTeam(TeamDTO teamCreation) {
            Team team = new Team();
            team.setTeamName(teamCreation.getTeamName());
            team.setCharacterName(teamCreation.getCharacterName());
            team.setFinalReady(true);


            if (teamRepository.existsByCharacterName(team.getCharacterName())) {
                throw new IllegalArgumentException("Character name already exists");
            }
            if (teamRepository.existsByTeamName(team.getTeamName())) {
                throw new IllegalArgumentException("Team name already exists");
            }
            return teamRepository.save(team);

        }

        public void deleteTeam(Long id) {
            teamRepository.deleteById(id);
        }

        public Team updateTeam(Long id, TeamDTO teamCreation) throws EntityNotFoundException, IllegalArgumentException {
            Team team = teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

            if (teamCreation.getTeamName() != null) {
                team.setTeamName(teamCreation.getTeamName());
            }

            if (teamCreation.getCharacterName() != null) {
                team.setCharacterName(teamCreation.getCharacterName());
            }

            if (teamRepository.existsByCharacterName(team.getCharacterName())) {
                throw new IllegalArgumentException("Character name already exists");
            }
            if (teamRepository.existsByTeamName(team.getTeamName())) {
                throw new IllegalArgumentException("Team name already exists");
            }

            return teamRepository.save(team);
        }
}
