package de.fsr.mariokart_backend.settings.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import de.fsr.mariokart_backend.translation.Translations;

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
    public static final int DEFAULT_FINAL_GAMES_COUNT = 6;
    public static final int DEFAULT_ROUND_MINUTES = 20;
    public static final int DEFAULT_RANKING_HIDDEN_ROUNDS = 1;
    public static final int DEFAULT_MAX_ANSWERS_PER_TEAM = 4;
    public static final int DEFAULT_SURVEY_KEY_DAYS = 2;

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

    @Column(nullable = false)
    private int finalGamesCount = DEFAULT_FINAL_GAMES_COUNT;

    @Column(nullable = false)
    private int roundMinutes = DEFAULT_ROUND_MINUTES;

    @Column(nullable = false)
    private int rankingHiddenRounds = DEFAULT_RANKING_HIDDEN_ROUNDS;

    @Column(nullable = false)
    private int maxAnswersPerTeam = DEFAULT_MAX_ANSWERS_PER_TEAM;

    @Column(nullable = false)
    private int surveyKeyDays = DEFAULT_SURVEY_KEY_DAYS;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_switch", joinColumns = @JoinColumn(name = "tournament_ID"))
    @OrderColumn(name = "switch_index")
    private List<SwitchConfig> switches = new ArrayList<>();

    // Floor plan as JSON
    @Column(columnDefinition = "TEXT")
    private String floorPlan;

    // Program of the event as JSON
    @Column(columnDefinition = "TEXT")
    private String program;

    @Column(columnDefinition = "TEXT")
    private String howToPlay;

    private String ocrProvider;

    private boolean relativePoints;

    @ColumnDefault("'de'")
    @Column(nullable = false)
    private String defaultLanguage = Translations.DEFAULT_LANGUAGE;

    public Tournament(Long id, boolean tournamentOpen, boolean registrationOpen, int maxGamesCount,
            SurveyKeyMode surveyKeyMode) {
        this.id = id;
        this.tournamentOpen = tournamentOpen;
        this.registrationOpen = registrationOpen;
        this.maxGamesCount = maxGamesCount;
        this.surveyKeyMode = surveyKeyMode;
    }
}
