package de.fsr.mariokart_backend.notification.service;

import java.security.GeneralSecurityException;
import java.security.Security;

import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.notification.model.PushSubscription;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Service
public class NotificationSendService {

    private PushService pushService;

    @Value("${vapid.public.key}")
    private String publicKey;

    @Value("${vapid.private.key}")
    private String privateKey;

    public NotificationSendService() {

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @PostConstruct
    public void init() throws GeneralSecurityException {
        String contactEmail = System.getenv("VAPID_CONTACT_EMAIL");
        if (contactEmail == null || contactEmail.isEmpty()) {
            contactEmail = "mailto:example@yourdomain.org"; // Fallback
        }
        this.pushService = new PushService(publicKey, privateKey, contactEmail);
    }

    public void sendNotification(PushSubscription subscription, String payload) throws Exception {
        Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                payload);

        // pushService.send(notification);
        HttpResponse response = pushService.send(notification);
        // TODO: remove print statement
        System.out.println(response);
    }

}