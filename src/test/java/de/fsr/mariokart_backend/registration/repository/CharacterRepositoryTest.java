package de.fsr.mariokart_backend.registration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
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
class CharacterRepositoryTest extends PostgresTestBase {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void findByCharacterNameAndTeamStateQueriesWork() {
        Character freeCharacter = TestDataFactory.character("Mario");
        characterRepository.save(freeCharacter);

        Character linkedCharacter = TestDataFactory.character("Luigi");
        characterRepository.save(linkedCharacter);

        Team team = TestDataFactory.team("Speedsters", linkedCharacter);
        linkedCharacter.setTeam(team);
        teamRepository.save(team);

        assertThat(characterRepository.findByCharacterName("Mario")).contains(freeCharacter);
        assertThat(characterRepository.findByCharacterName("Peach")).isEmpty();

        assertThat(characterRepository.findByTeamIsNull())
                .extracting(Character::getCharacterName)
                .contains("Mario");

        assertThat(characterRepository.findByTeamIsNotNull())
                .extracting(Character::getCharacterName)
                .contains("Luigi");
    }
}
