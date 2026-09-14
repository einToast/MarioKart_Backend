package de.fsr.mariokart_backend.settings.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class TournamentRepositoryTest extends PostgresTestBase {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Test
    void saveAndLoadTournamentWorks() {
        Tournament tournament = new Tournament(null, true, true, 6);
        Tournament saved = tournamentRepository.save(tournament);

        assertThat(saved.getId()).isNotNull();
        assertThat(tournamentRepository.findById(saved.getId())).contains(saved);
        assertThat(tournamentRepository.findAll()).hasSize(1);
    }
}
