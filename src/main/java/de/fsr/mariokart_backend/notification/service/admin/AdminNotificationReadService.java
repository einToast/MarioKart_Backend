package de.fsr.mariokart_backend.notification.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminNotificationReadService {

    private final PushSubscriptionRepository subscriptionRepository;

    public long getSubscriptionCount() {
        return subscriptionRepository.count();
    }
}
