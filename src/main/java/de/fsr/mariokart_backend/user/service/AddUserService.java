package de.fsr.mariokart_backend.user.service;

import de.fsr.mariokart_backend.user.model.User;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AddUserService {
    @Value("${user.name}")
    private String username;

    @Value("${user.password}")
    private String password;

    private final UserService userService;

    public AddUserService(UserService userService) {
        this.userService = userService;
    }

    public void addUser(){
        if (userService.getUsers().isEmpty()) {
            User user = new User(username, true);
            user.setPassword(password);
            userService.createAndRegisterIfNotExist(user);
        }
    }
}
