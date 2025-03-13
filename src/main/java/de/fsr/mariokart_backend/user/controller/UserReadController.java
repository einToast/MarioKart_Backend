package de.fsr.mariokart_backend.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.user.model.dto.UserDTO;
import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserReadController {

    private final UserService userService;

    @GetMapping
    public List<UserDTO> getUsers() {
        return UserDTO.fromUserList(userService.getUsers());
    }
}