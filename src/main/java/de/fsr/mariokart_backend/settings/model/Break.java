package de.fsr.mariokart_backend.settings.model;

import de.fsr.mariokart_backend.match_plan.model.Round;
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

    //    @OneToOne(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    private Break breakTime;
    @OneToOne(mappedBy = "breakTime", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Round round;


}
