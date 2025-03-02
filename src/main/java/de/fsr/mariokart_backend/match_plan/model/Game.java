package de.fsr.mariokart_backend.match_plan.model;

import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.fsr.mariokart_backend.registration.model.Team;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // @ManyToOne
    // @JoinColumn(name = "team_id", nullable = false)
    // private Team team;

    @ManyToOne
    @JoinColumn(name = "round_ID")
    @JsonBackReference
    private Round round;

    @OneToMany(mappedBy = "game", orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<Points> points;

    public Set<Team> getTeams() {
        if (points == null)
            return null;

        return points.stream().map(Points::getTeam).collect(Collectors.toSet());
    }
}
