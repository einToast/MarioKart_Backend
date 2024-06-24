package de.fsr.mariokart_backend.user.model.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCreationDTO {
    String username;
    boolean isAdmin;

    public void setIsAdmin(boolean isAdmin){
        this.isAdmin = isAdmin;
    }
}
