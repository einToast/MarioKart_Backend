package de.fsr.mariokart_backend.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserPasswordsDTO {
    String password;
    String passwordConfirm;
}
