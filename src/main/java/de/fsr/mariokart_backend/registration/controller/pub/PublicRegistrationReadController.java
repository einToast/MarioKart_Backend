package de.fsr.mariokart_backend.registration.controller.pub;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.REGISTRATION)
public class PublicRegistrationReadController {

    private final PublicRegistrationReadService publicRegistrationReadService;

    @GetMapping
    public List<TeamReturnDTO> getTeams() {
        return publicRegistrationReadService.getTeams();
    }

    @GetMapping("/sortedByGroupPoints")
    public List<TeamReturnDTO> getTeamsSortedByGroupPoints() {
        return publicRegistrationReadService.getTeamsSortedByGroupPoints();
    }

    @GetMapping("/sortedByTeamName")
    public List<TeamReturnDTO> getTeamsSortedByTeamName() {
        return publicRegistrationReadService.getTeamsSortedByTeamName();
    }

    @GetMapping("characters/available")
    public List<CharacterReturnDTO> getAvailableCharacters() {
        return publicRegistrationReadService.getAvailableCharacters();
    }

    @GetMapping("/notInRound/{roundId}")
    public List<TeamReturnDTO> getTeamsNotInRound(@PathVariable Long roundId) {
        return publicRegistrationReadService.getTeamsNotInRound(roundId);
    }
}