package de.fsr.mariokart_backend.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.user.model.UserToken;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    Optional<UserToken> findByToken(UUID uuid);
}