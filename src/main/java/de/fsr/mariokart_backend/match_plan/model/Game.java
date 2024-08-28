package de.fsr.mariokart_backend.match_plan.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import jakarta.persistence.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    @JoinColumn(name = "roundId")
    @JsonBackReference
    private Round round;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Points> points = new LinkedList<>();

    public Set<Team> getTeams() {
        if (points == null)
            return null;

        return points.stream().map(Points::getTeam).collect(Collectors.toSet());
    }
}

