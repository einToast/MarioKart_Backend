package de.fsr.mariokart_backend.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.dto.UserDTO;
import de.fsr.mariokart_backend.user.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserService {
    private final UserRepository userRepository;

    @CacheEvict(allEntries = true)
    public UserDTO updateUser(int userID) throws EntityNotFoundException {
        User user = getUser(userID);

        return new UserDTO(userRepository.save(user));
    }

    @Cacheable(key = "#username", sync = true)
    public User getUser(String username) throws EntityNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with this username not found."));
    }

    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with this ID not found."));
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @CacheEvict(allEntries = true)
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @CacheEvict(allEntries = true)
    public User createAndRegisterIfNotExist(User user) {
        Optional<User> userOptional = userRepository.getUserByUsername(user.getUsername());
        return userOptional.orElseGet(() -> userRepository.save(user));
    }
}