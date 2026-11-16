package de.fsr.mariokart_backend.settings.service.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.ocr.service.admin.AdminOcrReadService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.SwitchConfig;
import de.fsr.mariokart_backend.settings.model.SwitchTranslation;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.SwitchDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyUpdateService;
import de.fsr.mariokart_backend.translation.TranslationRebase;
import de.fsr.mariokart_backend.translation.Translations;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.node.ObjectNode;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "settings")
@CacheEvict(allEntries = true)
@Transactional(rollbackFor = Exception.class)
public class AdminSettingsUpdateService {

    public static final int MIN_FINAL_TEAMS = 2;
    public static final int MAX_FINAL_TEAMS = 8;
    public static final int MAX_SWITCHES = 16;
    public static final int MAX_SWITCH_NAME_LENGTH = 30;
    public static final int MAX_FLOOR_PLAN_LENGTH = 50_000;
    public static final int MAX_PROGRAM_LENGTH = 10_000;
    public static final int MAX_HOW_TO_PLAY_LENGTH = 10_000;
    public static final int MAX_SURVEY_KEY_DAYS = 400;

    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9a-fA-F]{6}");

    private final TournamentRepository tournamentRepository;
    private final PublicScheduleReadService publicScheduleReadService;
    private final AdminOcrReadService adminOcrReadService;
    private final WebSocketService webSocketService;
    private final AdminSurveyUpdateService adminSurveyUpdateService;
    private final TranslationRebase translationRebase;

    public TournamentDTO updateSettings(TournamentDTO tournamentDTO) throws RoundsAlreadyExistsException {
        Tournament tournament = tournamentRepository.findFirstByOrderByIdAsc().orElseThrow();
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
        if (tournamentDTO.getFinalGamesCount() != null
                && tournamentDTO.getFinalGamesCount() != tournament.getFinalGamesCount()) {
            requireFinalNotCreated("final games");
            tournament.setFinalGamesCount(requireAtLeast(tournamentDTO.getFinalGamesCount(), 1, "final games"));
        }
        if (tournamentDTO.getRoundMinutes() != null) {
            tournament.setRoundMinutes(requireAtLeast(tournamentDTO.getRoundMinutes(), 1, "minutes per round"));
        }
        if (tournamentDTO.getRankingHiddenRounds() != null) {
            tournament.setRankingHiddenRounds(requireAtLeast(tournamentDTO.getRankingHiddenRounds(), 0,
                    "rounds with a hidden ranking"));
        }
        if (tournamentDTO.getMaxAnswersPerTeam() != null) {
            tournament.setMaxAnswersPerTeam(requireAtLeast(tournamentDTO.getMaxAnswersPerTeam(), 1, "answers per team"));
        }
        if (tournamentDTO.getSurveyKeyDays() != null) {
            updateSurveyKeyDays(tournament, tournamentDTO.getSurveyKeyDays());
        }
        if (tournamentDTO.getSwitches() != null) {
            tournament.setSwitches(toSwitchConfigs(tournamentDTO.getSwitches()));
        }
        if (tournamentDTO.getFloorPlan() != null) {
            updateFloorPlan(tournament, tournamentDTO.getFloorPlan());
        }
        if (tournamentDTO.getProgram() != null) {
            updateProgram(tournament, tournamentDTO.getProgram());
        }
        if (tournamentDTO.getHowToPlay() != null) {
            updateHowToPlay(tournament, tournamentDTO.getHowToPlay());
        }
        if (tournamentDTO.getOcrProvider() != null) {
            updateOcrProvider(tournament, tournamentDTO.getOcrProvider());
        }
        if (tournamentDTO.getRelativePoints() != null) {
            tournament.setRelativePoints(tournamentDTO.getRelativePoints());
        }
        if (tournamentDTO.getDefaultLanguage() != null
                && !tournamentDTO.getDefaultLanguage().equals(tournament.getDefaultLanguage())) {
            updateDefaultLanguage(tournament, tournamentDTO.getDefaultLanguage());
        }

        TournamentDTO savedSettings = new TournamentDTO(tournamentRepository.save(tournament));
        webSocketService.sendMessage("/topic/settings", "update");
        return savedSettings;
    }

    private static int requireAtLeast(int value, int min, String name) {
        if (value < min) {
            throw new IllegalArgumentException("The %s have to be at least %d.".formatted(name, min));
        }
        return value;
    }

    private void updateSurveyKeyDays(Tournament tournament, int surveyKeyDays) {
        if (surveyKeyDays < 1 || surveyKeyDays > MAX_SURVEY_KEY_DAYS) {
            throw new IllegalArgumentException(
                    "Survey keys are valid for 1 to %d days, browsers drop longer cookies.".formatted(MAX_SURVEY_KEY_DAYS));
        }
        tournament.setSurveyKeyDays(surveyKeyDays);
    }

    private void requireFinalNotCreated(String name) throws RoundsAlreadyExistsException {
        if (publicScheduleReadService.isFinalScheduleCreated()) {
            throw new RoundsAlreadyExistsException("Final schedule already created. Can't change the %s.".formatted(name));
        }
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
            switchConfigs.add(new SwitchConfig(name, switchDTO.getColor(), toSwitchTranslations(switchDTO.getTranslations())));
        }
        return switchConfigs;
    }

    private Map<String, SwitchTranslation> toSwitchTranslations(Map<String, SwitchTranslation> translations) {
        return new HashMap<>(Translations.sanitize(translations, translation -> {
            String name = Translations.text(translation.name(), MAX_SWITCH_NAME_LENGTH, "switch names");
            return name == null ? null : new SwitchTranslation(name);
        }));
    }

    private void updateFloorPlan(Tournament tournament, String floorPlan) {
        if (floorPlan.length() > MAX_FLOOR_PLAN_LENGTH) {
            throw new IllegalArgumentException("The floor plan is too large.");
        }
        tournament.setFloorPlan(floorPlan.isBlank() ? null : floorPlan);
    }

    private void updateProgram(Tournament tournament, String program) {
        if (program.length() > MAX_PROGRAM_LENGTH) {
            throw new IllegalArgumentException("The program is too large.");
        }
        tournament.setProgram(program.isBlank() ? null : program);
    }

    private void updateHowToPlay(Tournament tournament, String howToPlay) {
        if (howToPlay.length() > MAX_HOW_TO_PLAY_LENGTH) {
            throw new IllegalArgumentException("The how-to-play page is too large.");
        }
        tournament.setHowToPlay(howToPlay.isBlank() ? null : howToPlay);
    }

    private void updateDefaultLanguage(Tournament tournament, String language) {
        if (!Translations.isLanguageCode(language)) {
            throw new IllegalArgumentException("The default language needs a language code like \"en\".");
        }
        String previous = tournament.getDefaultLanguage();
        tournament.setSwitches(new ArrayList<>(tournament.getSwitches().stream()
                .map(switchConfig -> rebaseSwitch(switchConfig, previous, language))
                .toList()));
        tournament.setFloorPlan(translationRebase.rebaseJson(tournament.getFloorPlan(), previous, language));
        tournament.setProgram(translationRebase.rebaseJson(tournament.getProgram(), previous, language));
        tournament.setHowToPlay(translationRebase.rebaseJson(tournament.getHowToPlay(), previous, language));
        adminSurveyUpdateService.rebaseTranslations(previous, language);
        tournament.setDefaultLanguage(language);
    }

    private SwitchConfig rebaseSwitch(SwitchConfig switchConfig, String from, String to) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", switchConfig.getName());
        ObjectNode node = translationRebase.toNode(fields, switchConfig.getTranslations());
        translationRebase.rebaseTree(node, from, to);
        return new SwitchConfig(translationRebase.text(node, "name"), switchConfig.getColor(),
                translationRebase.translationsOf(node, new TypeReference<Map<String, SwitchTranslation>>() {
                }));
    }

    private void updateOcrProvider(Tournament tournament, String ocrProvider) {
        if (ocrProvider.isBlank()) {
            tournament.setOcrProvider(null);
            return;
        }
        String provider = ocrProvider.strip();
        if (!adminOcrReadService.getProviders().contains(provider)) {
            throw new IllegalArgumentException("There is no OCR provider with this name.");
        }
        tournament.setOcrProvider(provider);
    }
}
