package de.fsr.mariokart_backend.settings.service.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.SwitchConfig;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.SwitchDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "settings")
@CacheEvict(allEntries = true)
public class AdminSettingsUpdateService {

    public static final int MIN_FINAL_TEAMS = 2;
    public static final int MAX_FINAL_TEAMS = 8;
    public static final int MAX_SWITCHES = 16;
    public static final int MAX_SWITCH_NAME_LENGTH = 30;
    public static final int MAX_FLOOR_PLAN_LENGTH = 50_000;

    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9a-fA-F]{6}");

    private final TournamentRepository tournamentRepository;
    private final PublicScheduleReadService publicScheduleReadService;

    public TournamentDTO updateSettings(TournamentDTO tournamentDTO) throws RoundsAlreadyExistsException {
        Tournament tournament = tournamentRepository.findAll().getFirst();
        if (tournamentDTO.getRegistrationOpen() != null && tournamentDTO.getRegistrationOpen()
                && publicScheduleReadService.isScheduleCreated()) {
            throw new RoundsAlreadyExistsException("Matches already exist. Can't open registration.");
        }

        if (tournamentDTO.getTournamentOpen() != null) {
            tournament.setTournamentOpen(tournamentDTO.getTournamentOpen());
        }
        if (tournamentDTO.getRegistrationOpen() != null) {
            tournament.setRegistrationOpen(tournamentDTO.getRegistrationOpen());
        }
        if (tournamentDTO.getMaxGamesCount() != null) {
            tournament.setMaxGamesCount(tournamentDTO.getMaxGamesCount());
        }
        if (tournamentDTO.getSurveyKeyMode() != null) {
            tournament.setSurveyKeyMode(tournamentDTO.getSurveyKeyMode());
        }
        if (tournamentDTO.getFinalTeamsCount() != null
                && tournamentDTO.getFinalTeamsCount() != tournament.getFinalTeamsCount()) {
            updateFinalTeamsCount(tournament, tournamentDTO.getFinalTeamsCount());
        }
        if (tournamentDTO.getSwitches() != null) {
            tournament.setSwitches(toSwitchConfigs(tournamentDTO.getSwitches()));
        }
        if (tournamentDTO.getFloorPlan() != null) {
            updateFloorPlan(tournament, tournamentDTO.getFloorPlan());
        }

        return new TournamentDTO(tournamentRepository.save(tournament));
    }

    private void updateFinalTeamsCount(Tournament tournament, int finalTeamsCount)
            throws RoundsAlreadyExistsException {
        if (finalTeamsCount < MIN_FINAL_TEAMS || finalTeamsCount > MAX_FINAL_TEAMS) {
            throw new IllegalArgumentException(
                    "The final needs between %d and %d teams.".formatted(MIN_FINAL_TEAMS, MAX_FINAL_TEAMS));
        }
        if (publicScheduleReadService.isFinalScheduleCreated()) {
            throw new RoundsAlreadyExistsException("Final schedule already created. Can't change the final teams.");
        }
        tournament.setFinalTeamsCount(finalTeamsCount);
    }

    private List<SwitchConfig> toSwitchConfigs(List<SwitchDTO> switches) {
        if (switches.size() > MAX_SWITCHES) {
            throw new IllegalArgumentException("There can be at most %d switches.".formatted(MAX_SWITCHES));
        }

        List<SwitchConfig> switchConfigs = new ArrayList<>();
        for (SwitchDTO switchDTO : switches) {
            String name = switchDTO.getName() == null ? "" : switchDTO.getName().trim();
            if (name.isEmpty() || name.length() > MAX_SWITCH_NAME_LENGTH) {
                throw new IllegalArgumentException(
                        "Switch names need between 1 and %d characters.".formatted(MAX_SWITCH_NAME_LENGTH));
            }
            if (switchDTO.getColor() == null || !HEX_COLOR.matcher(switchDTO.getColor()).matches()) {
                throw new IllegalArgumentException("Switch colors have to be hex colors like #9DAEDA.");
            }
            switchConfigs.add(new SwitchConfig(name, switchDTO.getColor()));
        }
        return switchConfigs;
    }

    // A blank floor plan removes the stored one
    private void updateFloorPlan(Tournament tournament, String floorPlan) {
        if (floorPlan.length() > MAX_FLOOR_PLAN_LENGTH) {
            throw new IllegalArgumentException("The floor plan is too large.");
        }
        tournament.setFloorPlan(floorPlan.isBlank() ? null : floorPlan);
    }
}
