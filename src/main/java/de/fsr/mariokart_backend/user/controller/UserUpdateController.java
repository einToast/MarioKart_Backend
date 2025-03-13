package de.fsr.mariokart_backend.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.user.exception.PasswordMismatchException;
import de.fsr.mariokart_backend.user.exception.TokenExpiredException;
import de.fsr.mariokart_backend.user.exception.TokenNotFoundException;
import de.fsr.mariokart_backend.user.model.dto.UpdateUserDTO;
import de.fsr.mariokart_backend.user.model.dto.UserDTO;
import de.fsr.mariokart_backend.user.model.dto.UserPasswordsDTO;
import de.fsr.mariokart_backend.user.service.AuthenticationService;
import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserUpdateController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @ResponseBody
    @PutMapping("register/{token}")
    public ResponseEntity<UserDTO> registerUserPassword(@RequestBody UserPasswordsDTO userPasswords,
            @PathVariable String token) {
        try {
            UserDTO userDTO = new UserDTO(authenticationService.registerUser(userPasswords, token));
            return ResponseEntity.ok(userDTO);
        } catch (TokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (PasswordMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{ID}")
    @ResponseBody
    public ResponseEntity<UserDTO> updateUser(@PathVariable int ID, @RequestBody UpdateUserDTO updateUser) {
        try {
            return ResponseEntity.ok(userService.updateUser(ID, updateUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}