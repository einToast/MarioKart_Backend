package de.fsr.mariokart_backend.settings.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
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
@Table(name = "tournament")
public class Tournament {

    public static final int DEFAULT_FINAL_TEAMS_COUNT = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean tournamentOpen;

    private boolean registrationOpen;

    private int maxGamesCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurveyKeyMode surveyKeyMode = SurveyKeyMode.DISABLED;

    @ColumnDefault("4")
    @Column(nullable = false)
    private int finalTeamsCount = DEFAULT_FINAL_TEAMS_COUNT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_switch", joinColumns = @JoinColumn(name = "tournament_ID"))
    @OrderColumn(name = "switch_index")
    private List<SwitchConfig> switches = new ArrayList<>();

    // Floor plan as JSON
    @Column(columnDefinition = "TEXT")
    private String floorPlan;

    // Programme of the event as JSON
    @Column(columnDefinition = "TEXT")
    private String program;

    public Tournament(Long id, boolean tournamentOpen, boolean registrationOpen, int maxGamesCount,
            SurveyKeyMode surveyKeyMode) {
        this.id = id;
        this.tournamentOpen = tournamentOpen;
        this.registrationOpen = registrationOpen;
        this.maxGamesCount = maxGamesCount;
        this.surveyKeyMode = surveyKeyMode;
    }
}
