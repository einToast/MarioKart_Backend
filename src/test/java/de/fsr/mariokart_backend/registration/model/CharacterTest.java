package de.fsr.mariokart_backend.registration.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CharacterTest {

    @Test
    void removeTeamDetachesBothSidesWhenTeamExists() {
        Character character = new Character();
        Team team = new Team();
        team.setCharacter(character);
        character.setTeam(team);

        character.removeTeam();

        assertThat(character.getTeam()).isNull();
        assertThat(team.getCharacter()).isNull();
    }

    @Test
    void removeTeamDoesNothingWhenTeamIsNull() {
        Character character = new Character();

        character.removeTeam();

        assertThat(character.getTeam()).isNull();
    }
}
