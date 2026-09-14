package de.fsr.mariokart_backend.registration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;
import de.fsr.mariokart_backend.testsupport.TestDataFactory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class TeamRepositoryTest extends PostgresTestBase {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Test
    void existsByTeamNameAndFindByTeamNameWork() {
        Team team = saveTeam("Speedsters", true);

        assertThat(teamRepository.existsByTeamName("Speedsters")).isTrue();
        assertThat(teamRepository.findByTeamName("Speedsters")).contains(team);
    }

    @Test
    void findByFinalReadyTrueAndOrderByTeamNameAscWork() {
        saveTeam("Alpha", true);
        saveTeam("Beta", false);
        saveTeam("Gamma", true);

        List<Team> readyTeams = teamRepository.findByFinalReadyTrue();
        assertThat(readyTeams).extracting(Team::getTeamName)
                .containsExactlyInAnyOrder("Alpha", "Gamma");

        List<Team> orderedTeams = teamRepository.findAllByOrderByTeamNameAsc();
        assertThat(orderedTeams).extracting(Team::getTeamName)
                .containsExactly("Alpha", "Beta", "Gamma");
    }

    private Team saveTeam(String teamName, boolean finalReady) {
        Character character = TestDataFactory.character(teamName + "-char");
        characterRepository.save(character);

        Team team = TestDataFactory.team(teamName, character);
        team.setFinalReady(finalReady);
        character.setTeam(team);

        return teamRepository.save(team);
    }
}
