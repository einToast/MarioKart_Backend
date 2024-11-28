package de.fsr.mariokart_backend.user.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@NoArgsConstructor
@Getter
@Setter
public class AdminUserService {
    @Value("${user.name}")
    private String username;

    @Value("${user.password}")
    private String password;
}
