package de.fsr.mariokart_backend.notification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationRequestDTO {
    
    private String title;
    private String message;
}
