package de.fsr.mariokart_backend.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fsr.mariokart_backend.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    void deleteById(Integer ID);

    boolean existsByUsername(String username);

    Optional<User> getUserByUsername(String username);
}