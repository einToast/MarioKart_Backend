package de.fsr.mariokart_backend.registration.model;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @JoinColumn(name = "character_ID", nullable = false)
    private Character character;

    @Column(unique = true)
    private String teamName;

    private boolean finalReady;

    private boolean active;

    @OneToMany(mappedBy = "team", orphanRemoval = true, fetch = FetchType.EAGER)
    // @JsonManagedReference
    private Set<Points> points;

    public int getGroupPoints(int maxGames) {
        if (points == null)
            return 0;

        return points.stream()
                .sorted(Comparator.comparingLong(Points::getId))
                .limit(maxGames)
                .mapToInt(Points::getGroupPoints)
                .sum();
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

    public int getNumberOfgamesPlayed(int maxGames) {
        if (points == null)
            return 0;

        return (int) getGames().stream().filter(game -> game.getRound().isPlayed()).count();
    }

    public void removeCharacter() {
        if (this.character != null) {
            this.character.setTeam(null);
            this.character = null;
        }
    }

}
