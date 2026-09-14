package de.fsr.mariokart_backend.testsupport;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationRequestDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResponseDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResult;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static TournamentDTO openTournamentSettings() {
        return new TournamentDTO(true, true, 4);
    }

    public static Character character(String name) {
        Character character = new Character();
        character.setCharacterName(name);
        return character;
    }

    public static Team team(String teamName, Character character) {
        Team team = new Team();
        team.setTeamName(teamName);
        team.setCharacter(character);
        team.setFinalReady(true);
        team.setActive(true);
        return team;
    }

    public static TeamInputDTO teamInput(String teamName, String characterName) {
        return new TeamInputDTO(teamName, characterName);
    }

    public static TeamReturnDTO teamReturn(Long id, String teamName, String characterName) {
        return new TeamReturnDTO(id, teamName, new CharacterReturnDTO(1L, characterName), true, true, 0, 0, 0);
    }

    public static AuthenticationRequestDTO authRequest(String username, String password) {
        return new AuthenticationRequestDTO(username, password);
    }

    public static AuthenticationResult authResult(String token, User user) {
        return new AuthenticationResult(token, new AuthenticationResponseDTO(user));
    }

    public static User user(String username, boolean isAdmin) {
        User user = new User(username, isAdmin);
        user.setID(1);
        user.setPassword("secret");
        return user;
    }
}
