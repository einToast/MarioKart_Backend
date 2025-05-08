package de.fsr.mariokart_backend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.notification.model.PushSubscription;

import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByTeamId(Long teamId);
}