package de.fsr.mariokart_backend.user.repository;

import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    Optional<UserToken> findByToken(UUID uuid);
}