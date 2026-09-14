package de.fsr.mariokart_backend.e2e.testsupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.config.AuthCookieConstants;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.GameRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.repository.UserRepository;
import de.fsr.mariokart_backend.user.repository.UserTokenRepository;
import jakarta.servlet.http.Cookie;

public abstract class AbstractEndToEndApiSmokeTest extends PostgresTestBase {

    @SuppressWarnings("resource")
    private static final GenericContainer<?> SCHEDULE_CONTAINER = new GenericContainer<>(
            DockerImageName.parse("eintoast/mariokart_schedule:latest"))
            .withExposedPorts(8000)
            .waitingFor(Wait.forHttp("/healthcheck").forPort(8000));

    protected static final ObjectMapper JSON = new ObjectMapper();

    static {
        SCHEDULE_CONTAINER.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserTokenRepository userTokenRepository;

    @Autowired
    protected TournamentRepository tournamentRepository;

    @Autowired
    protected CharacterRepository characterRepository;

    @Autowired
    protected TeamRepository teamRepository;

    @Autowired
    protected RoundRepository roundRepository;

    @Autowired
    protected BreakRepository breakRepository;

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    protected PointsRepository pointsRepository;

    @Autowired
    protected QuestionRepository questionRepository;

    @Autowired
    protected AnswerRepository answerRepository;

    @Autowired
    protected PushSubscriptionRepository pushSubscriptionRepository;

    @MockitoBean
    protected NotificationSendService notificationSendService;

    @DynamicPropertySource
    static void registerScheduleGeneratorProperties(DynamicPropertyRegistry registry) {
        registry.add("SCHEDULE_PROTOCOL", () -> "http");
        registry.add("SCHEDULE_HOST", SCHEDULE_CONTAINER::getHost);
        registry.add("SCHEDULE_PORT", () -> SCHEDULE_CONTAINER.getMappedPort(8000));
    }

    @BeforeEach
    void setUpEndToEndApiSmokeTest() throws Exception {
        doNothing().when(notificationSendService).sendNotification(any(PushSubscription.class), anyString());
        clearCaches();
        resetDatabase();
        seedTournamentSettings();
        seedCharacters();
        seedAdminUser();
    }

    protected void clearCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    protected void resetDatabase() {
        userTokenRepository.deleteAll();
        pushSubscriptionRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        pointsRepository.deleteAll();
        gameRepository.deleteAll();
        roundRepository.deleteAll();
        breakRepository.deleteAll();
        teamRepository.deleteAll();
        characterRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected void seedTournamentSettings() {
        Tournament tournament = new Tournament();
        tournament.setTournamentOpen(true);
        tournament.setRegistrationOpen(true);
        tournament.setMaxGamesCount(6);
        tournamentRepository.save(tournament);
    }

    protected void seedCharacters() {
        try {
            List<String> characters = new AddCharacterService(null, null).getImageNames("media");
            characters.forEach(name -> characterRepository.save(new Character(null, name, null)));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load character names from static media.", exception);
        }
    }

    protected void seedAdminUser() {
        User adminUser = new User("admin", true);
        adminUser.setPassword("secret");
        userRepository.save(adminUser);
    }

    protected MockCookie loginAsAdmin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/public/user/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return authCookieFrom(loginResult);
    }

    protected MockCookie authCookieFrom(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie(AuthCookieConstants.AUTH_COOKIE_NAME);
        assertThat(cookie).isNotNull();
        return new MockCookie(cookie.getName(), cookie.getValue());
    }

    protected Team createTeamDirect(String teamName, String characterName) {
        Character character = characterRepository.findByCharacterName(characterName).orElseThrow();
        Team team = new Team();
        team.setTeamName(teamName);
        team.setCharacter(character);
        team.setFinalReady(true);
        team.setActive(true);
        character.setTeam(team);
        return teamRepository.save(team);
    }

    protected Round createRoundDirect(int roundNumber, boolean played, boolean finalGame) {
        Round round = new Round();
        round.setRoundNumber(roundNumber);
        round.setPlayed(played);
        round.setFinalGame(finalGame);
        round.setStartTime(LocalDateTime.now().plusMinutes(roundNumber));
        round.setEndTime(LocalDateTime.now().plusMinutes(roundNumber + 20L));
        return roundRepository.save(round);
    }

    protected void seedSixteenTeams() {
        List<String> characters = characterRepository.findAll().stream()
                .map(Character::getCharacterName)
                .limit(16)
                .toList();

        for (int index = 0; index < 16; index++) {
            createTeamDirect("Team " + (index + 1), characters.get(index));
        }
    }

    protected long jsonFieldAsLong(MvcResult result, String fieldName) throws Exception {
        JsonNode node = JSON.readTree(result.getResponse().getContentAsString());
        return node.path(fieldName).asLong();
    }

    protected long jsonElementFieldAsLong(MvcResult result, int index, String fieldName) throws Exception {
        JsonNode node = JSON.readTree(result.getResponse().getContentAsString());
        return node.get(index).path(fieldName).asLong();
    }
}
