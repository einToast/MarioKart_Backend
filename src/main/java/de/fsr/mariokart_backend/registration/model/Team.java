package de.fsr.mariokart_backend.registration.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
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
    private Set<Points> points;

    @JsonIgnore
    @ManyToMany(mappedBy = "teams")
    private Set<TeamQuestion> teamQuestions;

    @PreRemove
    public void removeTeamAssociations() {
        if (teamQuestions != null) {
            for (TeamQuestion question : new ArrayList<>(teamQuestions)) {
                question.getTeams().remove(this);
            }
            teamQuestions.clear();
        }
        if (character != null) {
            removeCharacter();
        }
    }

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

    public int getNumberOfGamesPlayed(int maxGames) {
        if (points == null)
            return 0;

        int gamesPlayed = (int) getGames().stream().filter(game -> game.getRound().isPlayed()).count();


        return gamesPlayed > maxGames ? maxGames : gamesPlayed;
    }

    public void removeCharacter() {
        if (this.character != null) {
            this.character.setTeam(null);
            this.character = null;
        }
    }

}
