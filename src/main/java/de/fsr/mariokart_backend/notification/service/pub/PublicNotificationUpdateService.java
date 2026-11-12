package de.fsr.mariokart_backend.notification.service.pub;

import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionLanguageDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.translation.Translations;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicNotificationUpdateService {

    private final PushSubscriptionRepository subscriptionRepository;

    public void updateLanguage(PushSubscriptionLanguageDTO update) {
        if (update == null || update.endpoint() == null) {
            return;
        }
        String language = Translations.languageOrDefault(update.language());
        List<PushSubscription> subscriptions = subscriptionRepository.findByEndpoint(update.endpoint());
        subscriptions.forEach(subscription -> subscription.setLanguage(language));
        subscriptionRepository.saveAll(subscriptions);
    }
}
