package de.fsr.mariokart_backend.registration.model;

import com.fasterxml.jackson.annotation.*;
import de.fsr.mariokart_backend.match_plan.model.Game;
import lombok.*;

import jakarta.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

import de.fsr.mariokart_backend.match_plan.model.Points;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "characterId")
    private Character character;

    @Column(unique = true)
    private String teamName;

    private boolean finalReady;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JsonManagedReference
    private Set<Points> points;

    public int getGroupPoints() {
        if (points == null)
            return 0;

        return points.stream().mapToInt(Points::getGroupPoints).sum();
    }

    public int getFinalPoints() {
        if (points == null)
            return 0;

        return points.stream().mapToInt(Points::getFinalPoints).sum();
    }

    public Set<Game> getGames() {
        if (points == null)
            return null;

        return points.stream().map(Points::getGame).collect(Collectors.toSet());
    }

}
