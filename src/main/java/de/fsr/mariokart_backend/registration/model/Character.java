package de.fsr.mariokart_backend.registration.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "character")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Character {

        @Getter
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true)
        private String characterName;

        @OneToOne(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//        @JsonManagedReference
        private Team team;

}