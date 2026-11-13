package de.fsr.mariokart_backend.seed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.CharacterNames;
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
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class EventSeedServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private PublicRegistrationCreateService publicRegistrationCreateService;
    @Mock
    private PublicSettingsReadService publicSettingsReadService;
    @Mock
    private PublicScheduleReadService publicScheduleReadService;
    @Mock
    private AdminSettingsUpdateService adminSettingsUpdateService;
    @Mock
    private AdminSurveyCreateService adminSurveyCreateService;
    @Mock
    private AdminScheduleCreateService adminScheduleCreateService;

    private EventSeedService eventSeedService;

    @BeforeEach
    void setUp() {
        eventSeedService = new EventSeedService(new DefaultResourceLoader(), JsonMapper.builder().build(),
                teamRepository, questionRepository, publicRegistrationCreateService, publicSettingsReadService,
                publicScheduleReadService, adminSettingsUpdateService, adminSurveyCreateService,
                adminScheduleCreateService);
    }

    @Test
    void seedWithoutEventCreatesNothing() {
        eventSeedService.seed(" ");

        verifyNoInteractions(adminSettingsUpdateService, publicRegistrationCreateService, adminSurveyCreateService,
                adminScheduleCreateService);
    }

    @Test
    void seedOfUnknownEventFails() {
        assertThatThrownBy(() -> eventSeedService.seed("does-not-exist"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classpath:seed/does-not-exist.json");
    }

    @ParameterizedTest
    @ValueSource(strings = { "demo", "registration" })
    void shippedEventsOnlyUseRegistrableTeams(String event) {
        List<TeamInputDTO> teams = eventSeedService.load(event).getTeams();

        assertThat(teams).isNotEmpty();
        assertThat(teams).extracting(TeamInputDTO::getCharacterName)
                .doesNotHaveDuplicates()
                .allMatch(CharacterNames.ALL::contains);
        assertThat(teams).extracting(TeamInputDTO::getTeamName).doesNotHaveDuplicates();
    }

    @Test
    void demoEventHasEnoughTeamsForItsSchedule() {
        EventSeedDTO demo = eventSeedService.load("demo");
        ScheduleInputDTO schedule = demo.getSchedule();

        assertThat(demo.getTeams()).hasSizeGreaterThanOrEqualTo(schedule.getNumFields() * schedule.getTeamsPerGame());
        assertThat(demo.getSettings().getSwitches()).hasSize(schedule.getNumFields());
    }

    @Test
    void demoEventStoresFloorPlanAndProgramAsJsonText() throws Exception {
        when(teamRepository.count()).thenReturn(1L);
        when(questionRepository.count()).thenReturn(1L);
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        eventSeedService.seed("demo");

        ArgumentCaptor<TournamentDTO> settings = ArgumentCaptor.forClass(TournamentDTO.class);
        verify(adminSettingsUpdateService).updateSettings(settings.capture());
        assertThat(settings.getValue().getFloorPlan()).startsWith("{\"version\":1,\"elements\":[");
        assertThat(settings.getValue().getProgram()).startsWith("[{\"icon\":\"megaphone\"");
        assertThat(settings.getValue().getSwitches()).hasSize(4);
    }

    @Test
    void teamsAreRegisteredWhileRegistrationIsOpenAndTheSettingsAreRestoredAfterwards() throws Exception {
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, false, 0, null));

        eventSeedService.seed("registration");

        ArgumentCaptor<TournamentDTO> settings = ArgumentCaptor.forClass(TournamentDTO.class);
        InOrder order = inOrder(adminSettingsUpdateService, publicRegistrationCreateService);
        order.verify(adminSettingsUpdateService, times(2)).updateSettings(settings.capture());
        order.verify(publicRegistrationCreateService, times(21)).registerTeam(any());
        order.verify(adminSettingsUpdateService).updateSettings(settings.capture());

        assertThat(settings.getAllValues().get(1).getRegistrationOpen()).isTrue();
        assertThat(settings.getAllValues().get(2).getRegistrationOpen()).isFalse();
    }

    @Test
    void existingDataIsNotSeededAgain() throws Exception {
        when(teamRepository.count()).thenReturn(5L);
        when(questionRepository.count()).thenReturn(3L);
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        eventSeedService.seed("demo");

        verify(publicRegistrationCreateService, never()).registerTeam(any());
        verify(adminSurveyCreateService, never()).createQuestion(any(QuestionInputDTO.class));
        verify(adminScheduleCreateService, never()).createSchedule(any());
    }

    @Test
    void failingSectionsDoNotStopTheOthers() throws Exception {
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, false, 0, null));
        when(adminSettingsUpdateService.updateSettings(any()))
                .thenThrow(new IllegalArgumentException("The floor plan is too large."))
                .thenReturn(new TournamentDTO());
        when(publicRegistrationCreateService.registerTeam(any()))
                .thenThrow(new IllegalArgumentException("Character is already in a team"));
        when(adminScheduleCreateService.createSchedule(any()))
                .thenThrow(new RoundsAlreadyExistsException("Schedule already created"));

        eventSeedService.seed("demo");

        verify(publicRegistrationCreateService, times(21)).registerTeam(any());
        verify(adminSurveyCreateService, times(5)).createQuestion(any(QuestionInputDTO.class));
        verify(adminScheduleCreateService).createSchedule(any());
    }

    @Test
    void unreachableScheduleServiceDoesNotStopTheStartup() throws Exception {
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, false, 0, null));
        when(adminScheduleCreateService.createSchedule(any()))
                .thenThrow(new RuntimeException("Failed to send request to schedule generator"));

        eventSeedService.seed("demo");

        verify(adminScheduleCreateService).createSchedule(any());
    }
}
