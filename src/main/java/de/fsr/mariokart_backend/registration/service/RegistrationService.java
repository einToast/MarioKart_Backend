package de.fsr.mariokart_backend.registration.service;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.dto.TeamDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
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
        private final CharacterRepository characterRepository;

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

        public Team getTeamByName(String teamName) throws EntityNotFoundException{
            System.out.println(teamName);
            System.out.println(teamRepository.findByTeamName(teamName));
            return teamRepository.findByTeamName(teamName).orElseThrow(() -> new EntityNotFoundException("There is no team with this name."));
        }

        public List<Character> getCharacters() {
            return characterRepository.findAll();
        }


        public List<Character> getAvailableCharacters() {
            return characterRepository.findByTeamIsNull();
        }

        public List<Character> getTakenCharacters() {
            return characterRepository.findByTeamIsNotNull();
        }

        public Character getCharacterById(Long id) throws EntityNotFoundException{
            return characterRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no character with this ID."));
        }

        public Character getCharacterByName(String characterName) throws EntityNotFoundException{
            return characterRepository.findByCharacterName(characterName).orElseThrow(() -> new EntityNotFoundException("There is no character with this name."));
        }

        public Team addTeam(TeamDTO teamCreation) throws IllegalArgumentException, EntityNotFoundException{
            Team team = new Team();
            team.setTeamName(teamCreation.getTeamName());
            team.setCharacter(characterRepository.findByCharacterName(teamCreation.getCharacterName()).orElseThrow(() -> new EntityNotFoundException("There is no character with this name.")));

            team.setFinalReady(true);

            if (team.getCharacter().getTeam() != null) {
                throw new IllegalArgumentException("Character is already in a team");
            }
            if (teamRepository.existsByTeamName(team.getTeamName())) {
                throw new IllegalArgumentException("Team name already exists");
            }

            return teamRepository.save(team);

        }

        public Character addCharacter(Character character) {
            return characterRepository.save(character);
        }

        public List<Character> addCharacters(List<Character> characters) {
            return characterRepository.saveAll(characters);
        }

        public Team updateTeam(Long id, TeamDTO teamCreation) throws EntityNotFoundException, IllegalArgumentException {
            Team team = teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

            if (teamCreation.getTeamName() != null) {
                team.setTeamName(teamCreation.getTeamName());
            }


            if (teamCreation.getCharacterName() != null) {
                team.setCharacter(characterRepository.findByCharacterName(teamCreation.getCharacterName()).orElseThrow(() -> new EntityNotFoundException("There is no character with this name.")));
            }

            if (team.getCharacter().getTeam() != null) {
                throw new IllegalArgumentException("Character is already in a team");
            }

            if (teamRepository.existsByTeamName(team.getTeamName())) {
                throw new IllegalArgumentException("Team name already exists");
            }


            return teamRepository.save(team);
        }

        public void deleteTeam(Long id) {
            teamRepository.deleteById(id);
        }

}
