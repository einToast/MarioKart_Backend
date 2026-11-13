package de.fsr.mariokart_backend.notification.service.pub;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionEndpointDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicNotificationDeleteService {

    private final PushSubscriptionRepository subscriptionRepository;

    public void deleteSubscription(PushSubscriptionEndpointDTO subscription) {
        if (subscription == null || subscription.endpoint() == null) {
            return;
        }
        subscriptionRepository.deleteAll(subscriptionRepository.findByEndpoint(subscription.endpoint()));
    }
}
