package de.fsr.mariokart_backend.match_plan.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import jakarta.persistence.*;

import de.fsr.mariokart_backend.registration.model.Team;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "points")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Points {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int groupPoints;

    private int finalPoints;

    @ManyToOne
    @JoinColumn(name = "teamId", nullable = false)
//    @JsonBackReference
    private Team team;

    @ManyToOne
    @JoinColumn(name = "gameId", nullable = false)
    @JsonBackReference
    private Game game;

    @Override
    public String toString() {
        return game.getId() + " " + team.getId();
    }
}

