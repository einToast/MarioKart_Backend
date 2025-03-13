package de.fsr.mariokart_backend.user.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserDeleteController {

    private final UserService userService;

    @DeleteMapping("/{ID}")
    public void deleteUser(@PathVariable int ID) {
        userService.deleteUser(ID);
    }
}