package de.fsr.mariokart_backend.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class UserToken {
    @Id
    @UuidGenerator
    private UUID token;

    @NotNull
    private LocalDateTime expiresAt;

    @ManyToOne
    private User user;

    public UserToken(User user, LocalDateTime expiresAt){
        setUser(user);
        setExpiresAt(expiresAt);
    }
}