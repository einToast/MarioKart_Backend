package de.fsr.mariokart_backend.notification.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionInputDTO;

@Service
public class NotificationInputDTOService {

    public PushSubscription pushSubscriptionInputDTOToPushSubscription(PushSubscriptionInputDTO subscriptionInput) {
        PushSubscription subscription = new PushSubscription();
        subscription.setEndpoint(subscriptionInput.getEndpoint());
        subscription.setP256dh(subscriptionInput.getP256dh());
        subscription.setAuth(subscriptionInput.getAuth());
        subscription.setTeamId(subscriptionInput.getTeamId());
        return subscription;
    }
}
