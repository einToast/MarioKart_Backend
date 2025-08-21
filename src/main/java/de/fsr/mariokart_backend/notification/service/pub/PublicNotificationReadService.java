package de.fsr.mariokart_backend.notification.service.pub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PublicNotificationReadService {

    @Value("${vapid.public.key}")
    private String publicKey;

    public String getPublicKey() {
        return publicKey;
    }
}
