package de.fsr.mariokart_backend.seed.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationCreateService;
import de.fsr.mariokart_backend.schedule.model.dto.ScheduleInputDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleCreateService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.seed.model.dto.EventSeedDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyCreateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@AllArgsConstructor
public class EventSeedService {

    private static final String SEED_LOCATION = "classpath:seed/%s.json";

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final TeamRepository teamRepository;
    private final QuestionRepository questionRepository;
    private final PublicRegistrationCreateService publicRegistrationCreateService;
    private final PublicSettingsReadService publicSettingsReadService;
    private final PublicScheduleReadService publicScheduleReadService;
    private final AdminSettingsUpdateService adminSettingsUpdateService;
    private final AdminSurveyCreateService adminSurveyCreateService;
    private final AdminScheduleCreateService adminScheduleCreateService;
    private final SeedGroupPhaseService seedGroupPhaseService;

    public void seed(String event) {
        if (!StringUtils.hasText(event)) {
            return;
        }
        EventSeedDTO seed = load(event.strip());
        log.info("Seeding event '{}'", event.strip());

        seedSettings(seed);
        seedTeams(seed.getTeams());
        seedSurvey(seed.getSurvey());
        if (seedSchedule(seed.getSchedule()) && Boolean.TRUE.equals(seed.getGroupPhasePlayed())) {
            seedGroupPhaseService.playGroupPhase();
        }
    }

    public EventSeedDTO load(String event) {
        return load(event, new HashSet<>());
    }

    private EventSeedDTO load(String event, Set<String> loading) {
        if (!loading.add(event)) {
            throw new IllegalArgumentException("The event seed " + event + " extends itself");
        }
        EventSeedDTO seed = read(event);
        return seed.getExtendsEvent() == null ? seed : extend(load(seed.getExtendsEvent().strip(), loading), seed);
    }

    private static EventSeedDTO extend(EventSeedDTO base, EventSeedDTO seed) {
        return new EventSeedDTO(null,
                or(seed.getSettings(), base.getSettings()),
                or(seed.getFloorPlan(), base.getFloorPlan()),
                or(seed.getProgram(), base.getProgram()),
                or(seed.getHowToPlay(), base.getHowToPlay()),
                or(seed.getTeams(), base.getTeams()),
                or(seed.getSurvey(), base.getSurvey()),
                or(seed.getSchedule(), base.getSchedule()),
                or(seed.getGroupPhasePlayed(), base.getGroupPhasePlayed()));
    }

    private static <T> T or(T value, T fallback) {
        return value != null ? value : fallback;
    }

    private EventSeedDTO read(String event) {
        String location = event.contains(":") ? event : SEED_LOCATION.formatted(event);
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalArgumentException("There is no event seed at " + location);
        }
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(input, EventSeedDTO.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the event seed at " + location, e);
        }
    }

    private void seedSettings(EventSeedDTO seed) {
        TournamentDTO settings = seed.getSettings() == null ? new TournamentDTO() : seed.getSettings();
        if (seed.getFloorPlan() != null) {
            settings.setFloorPlan(toJson(seed.getFloorPlan()));
        }
        if (seed.getProgram() != null) {
            settings.setProgram(toJson(seed.getProgram()));
        }
        if (seed.getHowToPlay() != null) {
            settings.setHowToPlay(toJson(seed.getHowToPlay()));
        }
        updateSettings(settings);
    }

    private String toJson(JsonNode node) {
        return objectMapper.writeValueAsString(node);
    }

    private void seedTeams(List<TeamInputDTO> teams) {
        if (teams == null || teams.isEmpty()) {
            return;
        }
        if (teamRepository.count() > 0) {
            log.info("Teams already exist, the seeded teams are skipped.");
            return;
        }

        TournamentDTO before = publicSettingsReadService.getSettings();
        updateSettings(new TournamentDTO(true, true, null, null));
        for (TeamInputDTO team : teams) {
            try {
                publicRegistrationCreateService.registerTeam(team);
            } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException
                    | RoundsAlreadyExistsException e) {
                log.warn("Seeded team '{}' not registered: {}", team.getTeamName(), e.getMessage());
            }
        }
        updateSettings(new TournamentDTO(before.getTournamentOpen(), before.getRegistrationOpen(), null, null));
    }

    private void seedSurvey(List<QuestionInputDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        if (questionRepository.count() > 0) {
            log.info("Questions already exist, the seeded survey is skipped.");
            return;
        }
        for (QuestionInputDTO question : questions) {
            try {
                adminSurveyCreateService.createQuestion(question);
            } catch (IllegalArgumentException e) {
                log.warn("Seeded question '{}' not created: {}", question.getQuestionText(), e.getMessage());
            }
        }
    }

    private boolean seedSchedule(ScheduleInputDTO schedule) {
        if (schedule == null) {
            return false;
        }
        if (publicScheduleReadService.isScheduleCreated()) {
            log.info("A schedule already exists, the seeded schedule is skipped.");
            return false;
        }
        try {
            adminScheduleCreateService.createSchedule(schedule);
            return true;
        } catch (RoundsAlreadyExistsException | NotEnoughTeamsException | EntityNotFoundException
                | NotificationNotSentException e) {
            log.warn("Seeded schedule not created: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Seeded schedule not created, is the schedule service running? {}", e.getMessage());
        }
        return false;
    }

    private void updateSettings(TournamentDTO settings) {
        try {
            adminSettingsUpdateService.updateSettings(settings);
        } catch (RoundsAlreadyExistsException | IllegalArgumentException e) {
            log.warn("Seeded settings not applied: {}", e.getMessage());
        }
    }
}
