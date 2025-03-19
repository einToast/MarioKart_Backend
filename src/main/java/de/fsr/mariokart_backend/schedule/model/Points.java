package de.fsr.mariokart_backend.schedule.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.fsr.mariokart_backend.registration.model.Team;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "points")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Points {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int groupPoints;

    private int finalPoints;

    @ManyToOne
    @JoinColumn(name = "team_ID")
    // @JsonBackReference
    private Team team;

    @ManyToOne
    @JoinColumn(name = "game_ID")
    @JsonBackReference
    private Game game;

    @Override
    public String toString() {
        return game.getId() + " " + team.getId();
    }
}
