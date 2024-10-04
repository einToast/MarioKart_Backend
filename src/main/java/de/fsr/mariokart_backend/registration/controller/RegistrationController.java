package de.fsr.mariokart_backend.registration.controller;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.service.RegistrationService;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/teams")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;


    @GetMapping
    public List<TeamReturnDTO> getTeams() {
        return registrationService.getTeams();
    }

    @GetMapping("/sortedByNormalPoints")
    public List<TeamReturnDTO> getTeamsSortedByNormalPoints() {
        return registrationService.getTeamsSortedByNormalPoints();
    }

    @GetMapping("/sortedByFinalPoints")
    public List<TeamReturnDTO> getTeamsSortedByFinalPoints() {
        return registrationService.getTeamsSortedByFinalPoints();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamReturnDTO> getTeamById(@PathVariable Long id) {
        try{
            return ResponseEntity.ok(registrationService.getTeamById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TeamReturnDTO> getTeamByName(@PathVariable String name) {
        try{
            return ResponseEntity.ok(registrationService.getTeamByName(name));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("characters")
    public List<CharacterReturnDTO> getCharacters() {
        return registrationService.getCharacters();
    }

    @GetMapping("characters/available")
    public List<CharacterReturnDTO> getAvailableCharacters() {
        return registrationService.getAvailableCharacters();
    }

    @GetMapping("characters/taken")
    public List<CharacterReturnDTO> getTakenCharacters() {
        return registrationService.getTakenCharacters();
    }

    @GetMapping("characters/{id}")
    public ResponseEntity<CharacterReturnDTO> getCharacterById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(registrationService.getCharacterById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("characters/name/{name}")
    public ResponseEntity<CharacterReturnDTO> getCharacterByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(registrationService.getCharacterByName(name));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping
    public ResponseEntity<TeamReturnDTO> addTeam(@RequestBody TeamInputDTO teamCreation) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.addTeam(teamCreation));
        } catch (RoundsAlreadyExistsException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<TeamReturnDTO> updateTeam(@PathVariable Long id, @RequestBody TeamInputDTO teamCreation) {
        try {
            return ResponseEntity.ok(registrationService.updateTeam(id, teamCreation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        registrationService.deleteTeam(id);
    }

    @DeleteMapping
    public void deleteAllTeams() {
        registrationService.deleteAllTeams();
    }
}
