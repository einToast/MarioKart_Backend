package de.fsr.mariokart_backend.registration.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import jakarta.persistence.*;
import java.util.Set;

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

}
