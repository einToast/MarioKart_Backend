package de.fsr.mariokart_backend.ocr.service.admin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.ocr.model.dto.OcrCharacterDTO;
import de.fsr.mariokart_backend.ocr.model.dto.OcrInputDTO;
import de.fsr.mariokart_backend.ocr.model.dto.OcrReturnDTO;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleReadService;
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class AdminOcrCreateService {

    private final AdminScheduleReadService adminScheduleReadService;
    private final AddCharacterService addCharacterService;

    private final RestClient ocrRestClient;
    private final ObjectMapper objectMapper;

    public OcrReturnDTO detectCharactersByRoundAndGame(OcrInputDTO ocrInputDTO, Long roundId, Long gameId)
            throws EntityNotFoundException {

        List<RoundReturnDTO> rounds = adminScheduleReadService.getRounds();
        System.out.println("Rounds: " + rounds);
        RoundReturnDTO round = adminScheduleReadService.getRoundById(roundId);

        GameReturnDTO game = round.getGames().stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst()
                .orElseThrow(
                        () -> new EntityNotFoundException("There is no game with this ID in the specified round."));

        List<String> characterNames = game.getTeams().stream()
                .filter(team -> team.isActive())
                .map(team -> team.getCharacter().getCharacterName())
                .toList();

        // The OCR service matches against the names shown in the game, so send those
        // and map them back afterwards.
        Map<String, String> characterNamesByIngameName = new LinkedHashMap<>();
        for (String characterName : characterNames) {
            characterNamesByIngameName.put(addCharacterService.getCorrectedName(characterName), characterName);
        }

        int numCharacters = characterNames.size();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("provider", ocrInputDTO.provider());
        requestBody.put("image", ocrInputDTO.image());
        requestBody.put("characters", List.copyOf(characterNamesByIngameName.keySet()));
        requestBody.put("min_characters", numCharacters);
        requestBody.put("max_characters", numCharacters);

        String response;
        try {
            response = ocrRestClient.post()
                    .uri("/detect")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to ocr service", e);
        }

        try {
            OcrReturnDTO result = objectMapper.treeToValue(objectMapper.readTree(response).path("result"),
                    OcrReturnDTO.class);
            return new OcrReturnDTO(result.characters().stream()
                    .map(character -> new OcrCharacterDTO(
                            characterNamesByIngameName.getOrDefault(character.name(), character.name()),
                            character.points(), character.place()))
                    .toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }

    }

}
