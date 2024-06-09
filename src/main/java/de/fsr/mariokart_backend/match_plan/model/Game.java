package de.fsr.mariokart_backend.match_plan.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import jakarta.persistence.*;
import java.util.Set;

import de.fsr.mariokart_backend.registration.model.Team;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "game")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String switchGame;

//    @ManyToOne
//    @JoinColumn(name = "team_id", nullable = false)
//    private Team team;

    @ManyToOne
    @JoinColumn(name = "roundId", nullable = false)
    @JsonBackReference
    private Round round;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<Points> points;
}

