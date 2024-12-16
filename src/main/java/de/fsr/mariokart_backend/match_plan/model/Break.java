package de.fsr.mariokart_backend.match_plan.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Break {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean breakEnded;

//    TODO: Parent-Child_Relationship the other way around
    @OneToOne(mappedBy = "breakTime", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Round round;


}
