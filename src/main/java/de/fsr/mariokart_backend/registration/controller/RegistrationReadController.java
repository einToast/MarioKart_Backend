package de.fsr.mariokart_backend.registration.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.RegistrationReadService;
import lombok.AllArgsConstructor;

@CrossOrigin
@RestController
@RequestMapping("/teams")
@AllArgsConstructor
public class RegistrationReadController {

    private final RegistrationReadService registrationReadService;

    @GetMapping
    public List<TeamReturnDTO> getTeams() {
        return registrationReadService.getTeams();
    }

    @GetMapping("/sortedByNormalPoints")
    public List<TeamReturnDTO> getTeamsSortedByGroupPoints() {
        return registrationReadService.getTeamsSortedByGroupPoints();
    }

    @GetMapping("/sortedByFinalPoints")
    public List<TeamReturnDTO> getTeamsSortedByFinalPoints() {
        return registrationReadService.getTeamsSortedByFinalPoints();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamReturnDTO> getTeamById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(registrationReadService.getTeamById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TeamReturnDTO> getTeamByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(registrationReadService.getTeamByName(name));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("characters")
    public List<CharacterReturnDTO> getCharacters() {
        return registrationReadService.getCharacters();
    }

    @GetMapping("characters/available")
    public List<CharacterReturnDTO> getAvailableCharacters() {
        return registrationReadService.getAvailableCharacters();
    }

    @GetMapping("characters/taken")
    public List<CharacterReturnDTO> getTakenCharacters() {
        return registrationReadService.getTakenCharacters();
    }

    @GetMapping("characters/{id}")
    public ResponseEntity<CharacterReturnDTO> getCharacterById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(registrationReadService.getCharacterById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("characters/name/{name}")
    public ResponseEntity<CharacterReturnDTO> getCharacterByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(registrationReadService.getCharacterByName(name));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/notInRound/{roundId}")
    public List<TeamReturnDTO> getTeamsNotInRound(@PathVariable Long roundId) {
        return registrationReadService.getTeamsNotInRound(roundId);
    }
}