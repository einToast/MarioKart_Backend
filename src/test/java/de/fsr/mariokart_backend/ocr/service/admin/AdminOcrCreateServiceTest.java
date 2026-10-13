package de.fsr.mariokart_backend.ocr.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.ocr.model.dto.OcrCharacterDTO;
import de.fsr.mariokart_backend.ocr.model.dto.OcrInputDTO;
import de.fsr.mariokart_backend.ocr.model.dto.OcrReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleReadService;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminOcrCreateServiceTest {

    private static final Long ROUND_ID = 6L;
    private static final Long GAME_ID = 3L;
    private static final OcrInputDTO INPUT = new OcrInputDTO("anthropic", "aW1hZ2U=");

    @Mock
    private AdminScheduleReadService adminScheduleReadService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient ocrRestClient;

    @Captor
    private ArgumentCaptor<Map<String, Object>> sentBody;

    private AdminOcrCreateService service;

    @BeforeEach
    void setUp() {
        // The names are converted by the real service, it needs its dependencies only to add characters
        service = new AdminOcrCreateService(adminScheduleReadService, new AddCharacterService(null, null),
                ocrRestClient, JsonMapper.builder().build());
    }

    private void stubGame(String... characterNames) throws EntityNotFoundException {
        Set<TeamReturnDTO> teams = new LinkedHashSet<>();
        for (int i = 0; i < characterNames.length; i++) {
            long id = i + 1L;
            teams.add(new TeamReturnDTO(id, "Team " + id, new CharacterReturnDTO(id, characterNames[i]), true, true,
                    0, 0, 0));
        }

        Set<GameReturnDTO> games = new LinkedHashSet<>();
        games.add(new GameReturnDTO(GAME_ID - 1, 0, Set.of(), Set.of()));
        games.add(new GameReturnDTO(GAME_ID, 1, teams, Set.of()));

        RoundReturnDTO round = new RoundReturnDTO();
        round.setId(ROUND_ID);
        round.setGames(games);
        when(adminScheduleReadService.getRoundById(ROUND_ID)).thenReturn(round);
    }

    // Also remembers the body that is sent to the OCR service
    private void stubOcrResponse(String response) {
        when(ocrRestClient.post().uri("/detect").body(sentBody.capture()).retrieve().body(String.class))
                .thenReturn(response);
    }

    @Test
    void sendsTheCharactersOfTheGameWithTheirIngameNames() throws Exception {
        stubGame("Donkey-Kong", "Katzen-Peach", "Wendy-O.-Koopa", "Bowser");
        stubOcrResponse("{\"result\":{\"characters\":[]}}");

        service.detectCharactersByRoundAndGame(INPUT, ROUND_ID, GAME_ID);

        assertThat(sentBody.getValue())
                .containsEntry("provider", "anthropic")
                .containsEntry("image", "aW1hZ2U=")
                .containsEntry("characters", List.of("Donkey Kong", "Katzen-Peach", "Wendy", "Bowser"));
    }

    @Test
    void asksForExactlyAsManyCharactersAsTheGameHasTeams() throws Exception {
        stubGame("Mario", "Luigi", "Peach");
        stubOcrResponse("{\"result\":{\"characters\":[]}}");

        service.detectCharactersByRoundAndGame(INPUT, ROUND_ID, GAME_ID);

        assertThat(sentBody.getValue())
                .containsEntry("min_characters", 3)
                .containsEntry("max_characters", 3);
    }

    @Test
    void returnsTheDetectedCharactersWithTheNamesOfTheTeams() throws Exception {
        stubGame("Donkey-Kong", "Katzen-Peach", "Wendy-O.-Koopa", "Bowser");
        stubOcrResponse("""
                {
                  "provider": "anthropic",
                  "model": "claude-haiku-5-5",
                  "result": {
                    "characters": [
                      {"name": "Bowser", "points": 17, "place": 8},
                      {"name": "Donkey Kong", "points": 7, "place": 12},
                      {"name": "Katzen-Peach", "points": 31},
                      {"name": "Wendy", "points": 13}
                    ]
                  },
                  "usage": {"input_tokens": 1200, "output_tokens": 80}
                }
                """);

        OcrReturnDTO result = service.detectCharactersByRoundAndGame(INPUT, ROUND_ID, GAME_ID);

        assertThat(result.characters()).containsExactly(
                new OcrCharacterDTO("Bowser", 17, 8),
                new OcrCharacterDTO("Donkey-Kong", 7, 12),
                new OcrCharacterDTO("Katzen-Peach", 31, null),
                new OcrCharacterDTO("Wendy-O.-Koopa", 13, null));
    }

    @Test
    void keepsTheNameOfADetectedCharacterThatIsNotInTheGame() throws Exception {
        stubGame("Mario", "Luigi");
        stubOcrResponse("{\"result\":{\"characters\":[{\"name\":\"Baby Daisy\",\"points\":30}]}}");

        OcrReturnDTO result = service.detectCharactersByRoundAndGame(INPUT, ROUND_ID, GAME_ID);

        assertThat(result.characters()).containsExactly(new OcrCharacterDTO("Baby Daisy", 30, null));
    }

    @Test
    void throwsWhenTheGameIsNotPartOfTheRound() throws Exception {
        stubGame("Mario", "Luigi");

        assertThatThrownBy(() -> service.detectCharactersByRoundAndGame(INPUT, ROUND_ID, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no game with this ID");

        verifyNoInteractions(ocrRestClient);
    }

    @Test
    void reportsWhenTheOcrServiceCannotBeReached() throws Exception {
        stubGame("Mario", "Luigi");
        RuntimeException cause = new RuntimeException("ocr down");
        when(ocrRestClient.post()).thenThrow(cause);

        assertThatThrownBy(() -> service.detectCharactersByRoundAndGame(INPUT, ROUND_ID, GAME_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send request to ocr service")
                .hasCause(cause);
    }

    @Test
    void reportsWhenTheAnswerOfTheOcrServiceCannotBeRead() throws Exception {
        stubGame("Mario", "Luigi");
        stubOcrResponse("{\"detail\":\"no result\"}");

        assertThatThrownBy(() -> service.detectCharactersByRoundAndGame(INPUT, ROUND_ID, GAME_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to parse JSON response");
    }
}
