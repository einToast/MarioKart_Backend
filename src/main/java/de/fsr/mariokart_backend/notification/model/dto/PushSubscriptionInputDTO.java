package de.fsr.mariokart_backend.notification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PushSubscriptionInputDTO {
    private String endpoint;
    private String p256dh;
    private String auth;
    private Long teamId;
}
