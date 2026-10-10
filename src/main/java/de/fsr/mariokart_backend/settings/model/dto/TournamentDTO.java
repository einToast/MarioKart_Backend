package de.fsr.mariokart_backend.settings.model.dto;

import java.util.List;

import de.fsr.mariokart_backend.settings.model.SurveyKeyMode;
import de.fsr.mariokart_backend.settings.model.Tournament;
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
    private List<SwitchDTO> switches;
    private String floorPlan;

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
        this.switches = tournament.getSwitches() == null ? List.of()
                : tournament.getSwitches().stream()
                        .map(switchConfig -> new SwitchDTO(switchConfig.getName(), switchConfig.getColor()))
                        .toList();
        this.floorPlan = tournament.getFloorPlan();
    }

    public int finalTeamsCountOrDefault() {
        return finalTeamsCount != null ? finalTeamsCount : Tournament.DEFAULT_FINAL_TEAMS_COUNT;
    }

    // Falls back to the one-based switch number while the switch has no name
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
