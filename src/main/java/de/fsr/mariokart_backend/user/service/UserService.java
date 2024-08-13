package de.fsr.mariokart_backend.user.service;

import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.user.repository.UserRepository;
import de.fsr.mariokart_backend.user.model.dto.UpdateUserDTO;
import de.fsr.mariokart_backend.user.model.dto.UserDTO;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProperties userProperties;

    public UserDTO updateUser(int userID, UpdateUserDTO updateUserStepGoal) throws EntityNotFoundException, IllegalArgumentException {
        User user = getUser(userID);

        return new UserDTO(userRepository.save(user));
    }


    public User getUser(String username) throws EntityNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User with this username not found."));
    }

    public User getUser(int ID) throws EntityNotFoundException {
        return userRepository.findById(ID).orElseThrow(() -> new EntityNotFoundException("User with this ID not found."));
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(int ID) {
        userRepository.deleteById(ID);
    }

    public boolean userExists(String username){
        return userRepository.existsByUsername(username);
    }

    public User createAndRegisterIfNotExist(User user){
        Optional<User> userOptional = userRepository.getUserByUsername(user.getUsername());
        return userOptional.orElseGet(() -> userRepository.save(user));
    }
}