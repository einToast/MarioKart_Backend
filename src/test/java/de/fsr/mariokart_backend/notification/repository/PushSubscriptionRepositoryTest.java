package de.fsr.mariokart_backend.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class PushSubscriptionRepositoryTest extends PostgresTestBase {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Test
    void findByTeamIdWorks() {
        PushSubscription first = new PushSubscription();
        first.setEndpoint("endpoint-a");
        first.setP256dh("key-a");
        first.setAuth("auth-a");
        first.setTeamId(11L);

        PushSubscription second = new PushSubscription();
        second.setEndpoint("endpoint-b");
        second.setP256dh("key-b");
        second.setAuth("auth-b");
        second.setTeamId(11L);

        PushSubscription otherTeam = new PushSubscription();
        otherTeam.setEndpoint("endpoint-c");
        otherTeam.setP256dh("key-c");
        otherTeam.setAuth("auth-c");
        otherTeam.setTeamId(12L);

        pushSubscriptionRepository.save(first);
        pushSubscriptionRepository.save(second);
        pushSubscriptionRepository.save(otherTeam);

        assertThat(pushSubscriptionRepository.findByTeamId(11L)).hasSize(2);
        assertThat(pushSubscriptionRepository.findByTeamId(999L)).isEmpty();
    }
}
