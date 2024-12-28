package de.fsr.mariokart_backend.registration.service;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationInputDTOService;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final TeamRepository teamRepository;
    private final CharacterRepository characterRepository;
    private final RoundRepository roundRepository;

    private final RegistrationInputDTOService registrationInputDTOService;
    private final RegistrationReturnDTOService registrationReturnDTOService;
    private final SettingsService settingsService;

    public List<TeamReturnDTO> getTeams() {
        return teamRepository.findAll().stream()
                                       .map(registrationReturnDTOService::teamToTeamReturnDTO)
                                       .toList();

    }

    public List<TeamReturnDTO> getTeamsSortedByGroupPoints() {
        List<TeamReturnDTO> teams = teamRepository.findAllByOrderByGroupPointsDesc().stream()
                                                               .map(registrationReturnDTOService::teamToTeamReturnDTO)
                                                               .toList();
        if (teams.isEmpty()) {
            teams = getTeams();
        }

        return teams;
    }

    public List<TeamReturnDTO> getTeamsSortedByFinalPoints() {
        List<TeamReturnDTO> teams = teamRepository.findAllByOrderByFinalPointsDescGroupPointsDesc().stream()
                                                               .map(registrationReturnDTOService::teamToTeamReturnDTO)
                                                               .toList();
        if (teams.isEmpty()) {
            teams = getTeams();
        }
        return teams;
//        return teamRepository.findAllByOrderByFinalPointsDescGroupPointsDesc().stream()
//                .map(registrationReturnDTOService::teamToTeamReturnDTO)
//                .toList();
    }

    public TeamReturnDTO getTeamById(Long id) throws EntityNotFoundException{
        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID.")));
//            return teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));
    }

    public TeamReturnDTO getTeamByName(String teamName) throws EntityNotFoundException{
        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.findByTeamName(teamName).orElseThrow(() -> new EntityNotFoundException("There is no team with this name.")));
//            return teamRepository.findByTeamName(teamName).orElseThrow(() -> new EntityNotFoundException("There is no team with this name."));
    }

    public List<CharacterReturnDTO> getCharacters() {
        return characterRepository.findAll().stream()
                                            .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                                            .toList();
//            return characterRepository.findAll();
    }


    public List<CharacterReturnDTO> getAvailableCharacters() {
        return characterRepository.findByTeamIsNull().stream()
                                                     .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                                                     .toList();
    }

    public List<CharacterReturnDTO> getTakenCharacters() {
        return characterRepository.findByTeamIsNotNull().stream()
                                                        .map(registrationReturnDTOService::characterToCharacterReturnDTO)
                                                        .toList();
    }

    public CharacterReturnDTO getCharacterById(Long id) throws EntityNotFoundException{
        return registrationReturnDTOService.characterToCharacterReturnDTO(characterRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no character with this ID.")));
//            return characterRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no character with this ID."));
    }

    public CharacterReturnDTO getCharacterByName(String characterName) throws EntityNotFoundException{
        return registrationReturnDTOService.characterToCharacterReturnDTO(characterRepository.findByCharacterName(characterName).orElseThrow(() -> new EntityNotFoundException("There is no character with this name.")));
//            return characterRepository.findByCharacterName(characterName).orElseThrow(() -> new EntityNotFoundException("There is no character with this name."));
    }

    public TeamReturnDTO addTeam(TeamInputDTO teamCreation) throws IllegalArgumentException, EntityNotFoundException, RoundsAlreadyExistsException {
        if (!settingsService.getSettings().getRegistrationOpen()) {
            throw new IllegalStateException("Registration is closed");
        }
        if (!settingsService.getSettings().getTournamentOpen()) {
            throw new IllegalStateException("Tournament is closed");
        }

        if (!roundRepository.findAll().isEmpty()) {
            throw new RoundsAlreadyExistsException("Match plan already exists");
        }

        Team team = registrationInputDTOService.teamInputDTOToTeam(teamCreation);

        team.setFinalReady(true);

        if (team.getCharacter().getTeam() != null) {
            throw new IllegalArgumentException("Character is already in a team");
        }
        if (teamRepository.existsByTeamName(team.getTeamName())) {
            throw new IllegalArgumentException("Team name already exists");
        }

        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.save(team));

    }

    public Character addCharacter(Character character) {
        return characterRepository.save(character);
    }

    public List<Character> addCharacters(List<Character> characters) {
        return characterRepository.saveAll(characters);
    }

    public TeamReturnDTO updateTeam(Long id, TeamInputDTO teamCreation) throws EntityNotFoundException, IllegalArgumentException {
        Team team = teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

        if (teamCreation.getTeamName() != null && !team.getTeamName().equals(teamCreation.getTeamName())) {
            team.setTeamName(teamCreation.getTeamName());
        }

        if (teamCreation.getCharacterName() != null && !team.getCharacter().getCharacterName().equals(teamCreation.getCharacterName())) {
            team.setCharacter(characterRepository.findByCharacterName(teamCreation.getCharacterName()).orElseThrow(() -> new EntityNotFoundException("There is no character with this name.")));
        }

        if (teamCreation.isFinalReady() != team.isFinalReady()) {
            team.setFinalReady(teamCreation.isFinalReady());
        }

        if (teamCreation.isActive() != team.isActive()) {
            team.setActive(teamCreation.isActive());
        }

        if (team.getCharacter().getTeam() != null && !team.getCharacter().getTeam().getId().equals(id)) {
            throw new IllegalArgumentException("Character is already in a team");
        }

        if (teamRepository.existsByTeamName(team.getTeamName()) && !teamRepository.findByTeamName(team.getTeamName()).get().getId().equals(id)) {
            throw new IllegalArgumentException("Team name already exists");
        }

        return registrationReturnDTOService.teamToTeamReturnDTO(teamRepository.save(team));
    }

    public void deleteTeam(Long id) throws RoundsAlreadyExistsException {
        if (!roundRepository.findAll().isEmpty()) {
            throw new RoundsAlreadyExistsException("Match plan already exists");
        }
        teamRepository.deleteById(id);
    }

    public void deleteAllTeams() throws RoundsAlreadyExistsException {
        if (!roundRepository.findAll().isEmpty()) {
            throw new RoundsAlreadyExistsException("Match plan already exists");
        }
        teamRepository.deleteAll();
    }
}
