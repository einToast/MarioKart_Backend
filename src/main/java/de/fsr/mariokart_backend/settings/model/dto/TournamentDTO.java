package de.fsr.mariokart_backend.settings.model.dto;

import java.util.List;
import java.util.Map;

import de.fsr.mariokart_backend.settings.model.SurveyKeyMode;
import de.fsr.mariokart_backend.settings.model.SwitchTranslation;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.translation.Translations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TournamentDTO {
    private Boolean tournamentOpen;
    private Boolean registrationOpen;
    private Integer maxGamesCount;
    private SurveyKeyMode surveyKeyMode;
    private Integer finalTeamsCount;
    private Integer finalGamesCount;
    private Integer roundMinutes;
    private Integer rankingHiddenRounds;
    private Integer maxAnswersPerTeam;
    private Integer surveyKeyDays;
    private List<SwitchDTO> switches;
    private String floorPlan;
    private String program;
    private String howToPlay;
    private String ocrProvider;
    private Boolean relativePoints;

    public TournamentDTO(Boolean tournamentOpen, Boolean registrationOpen, Integer maxGamesCount,
            SurveyKeyMode surveyKeyMode) {
        this.tournamentOpen = tournamentOpen;
        this.registrationOpen = registrationOpen;
        this.maxGamesCount = maxGamesCount;
        this.surveyKeyMode = surveyKeyMode;
    }

    public TournamentDTO(Tournament tournament) {
        this.tournamentOpen = tournament.isTournamentOpen();
        this.registrationOpen = tournament.isRegistrationOpen();
        this.maxGamesCount = tournament.getMaxGamesCount();
        this.surveyKeyMode = tournament.getSurveyKeyMode();
        this.finalTeamsCount = tournament.getFinalTeamsCount();
        this.finalGamesCount = tournament.getFinalGamesCount();
        this.roundMinutes = tournament.getRoundMinutes();
        this.rankingHiddenRounds = tournament.getRankingHiddenRounds();
        this.maxAnswersPerTeam = tournament.getMaxAnswersPerTeam();
        this.surveyKeyDays = tournament.getSurveyKeyDays();
        this.switches = tournament.getSwitches() == null ? List.of()
                : tournament.getSwitches().stream()
                        .map(switchConfig -> new SwitchDTO(switchConfig.getName(), switchConfig.getColor(),
                                copyTranslations(switchConfig.getTranslations())))
                        .toList();
        this.floorPlan = tournament.getFloorPlan();
        this.program = tournament.getProgram();
        this.howToPlay = tournament.getHowToPlay();
        this.ocrProvider = tournament.getOcrProvider();
        this.relativePoints = tournament.isRelativePoints();
    }

    private static Map<String, SwitchTranslation> copyTranslations(Map<String, SwitchTranslation> translations) {
        return translations == null ? Map.of() : Map.copyOf(translations);
    }

    public int finalTeamsCountOrDefault() {
        return finalTeamsCount != null ? finalTeamsCount : Tournament.DEFAULT_FINAL_TEAMS_COUNT;
    }

    public int finalGamesCountOrDefault() {
        return finalGamesCount != null ? finalGamesCount : Tournament.DEFAULT_FINAL_GAMES_COUNT;
    }

    public int roundMinutesOrDefault() {
        return roundMinutes != null ? roundMinutes : Tournament.DEFAULT_ROUND_MINUTES;
    }

    public int rankingHiddenRoundsOrDefault() {
        return rankingHiddenRounds != null ? rankingHiddenRounds : Tournament.DEFAULT_RANKING_HIDDEN_ROUNDS;
    }

    // Falls back to the one-based switch number while the switch has no name
    public String switchName(int switchIndex, String language) {
        String name = switchName(switchIndex);
        if (switches == null || switchIndex < 0 || switchIndex >= switches.size()
                || switches.get(switchIndex).getTranslations() == null) {
            return name;
        }
        SwitchTranslation translation = switches.get(switchIndex).getTranslations().get(language);
        return translation == null ? name : Translations.translatedOrDefault(translation.name(), name);
    }

    public String switchName(int switchIndex) {
        if (switches != null && switchIndex >= 0 && switchIndex < switches.size()) {
            String name = switches.get(switchIndex).getName();
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        return String.valueOf(switchIndex + 1);
    }
}
