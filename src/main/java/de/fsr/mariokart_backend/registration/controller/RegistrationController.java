package de.fsr.mariokart_backend.registration.controller;

import de.fsr.mariokart_backend.registration.model.dto.TeamDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
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
        public List<Team> getTeams() {
            return registrationService.getTeams();
        }

        @GetMapping("/sortedByNormalPoints")
        public List<Team> getTeamsSortedByNormalPoints() {
            return registrationService.getTeamsSortedByNormalPoints();
        }

        @GetMapping("/sortedByFinalPoints")
        public List<Team> getTeamsSortedByFinalPoints() {
            return registrationService.getTeamsSortedByFinalPoints();
        }

        @GetMapping("/{id}")
        public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
            try{
                return ResponseEntity.ok(registrationService.getTeamById(id));
            } catch (EntityNotFoundException e) {
                return ResponseEntity.notFound().build();
            }
        }

        @PostMapping
        public ResponseEntity<Team> addTeam(@RequestBody TeamDTO teamCreation) {
            try{
                return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.addTeam(teamCreation));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @PutMapping("/{id}")
        @ResponseBody
        public ResponseEntity<Team> updateTeam(@PathVariable Long id, @RequestBody  TeamDTO teamCreation) {
            try {
                return ResponseEntity.ok(registrationService.updateTeam(id, teamCreation));
            } catch (EntityNotFoundException e) {
                return ResponseEntity.notFound().build();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
}
