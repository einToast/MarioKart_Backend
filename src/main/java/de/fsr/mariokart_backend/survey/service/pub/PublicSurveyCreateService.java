package de.fsr.mariokart_backend.survey.service.pub;

import java.util.Map;

import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicSurveyCreateService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerInputDTOService answerInputDTOService;
    private final AnswerReturnDTOService answerReturnDTOService;
    private final TeamRepository teamRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_ANSWERS_PER_TEAM = 4;

    public AnswerReturnDTO submitAnswer(AnswerInputDTO answer, String userJson)
            throws EntityNotFoundException, JacksonException {

        Map<String, Object> userMap = null;

        if (userJson != null && !userJson.isEmpty()) {
            userMap = objectMapper.readValue(userJson, new TypeReference<Map<String, Object>>() {
            });
        } else {
            throw new IllegalArgumentException("User JSON is null or empty.");
        }

        Question question = questionRepository.findById(answer.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));

        if (!question.getActive() || !question.getVisible()) {
            throw new IllegalStateException("Question is not active or visible.");
        }

        final Team submittingTeam = teamRepository.findById(Long.valueOf(((Number) userMap.get("teamId")).longValue()))
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this id."));

        // Skip team answer limit check for free text questions
        if (!"FREE_TEXT".equals(answer.getAnswerType())) {
            long teamAnswerCount = answerRepository.findAll().stream()
                    .filter(a -> a.getQuestion().getId().equals(answer.getQuestionId()))
                    .filter(a -> submittingTeam.equals(a.getSubmittingTeam()))
                    .count();

            if (teamAnswerCount >= MAX_ANSWERS_PER_TEAM) {
                throw new IllegalArgumentException("Maximum number of answers per team reached.");
            }
        }

        if ("TEAM_ONE_FREE_TEXT".equals(answer.getAnswerType())) {
            // TODO: simplify
            boolean hasAnswered = answerRepository.findAll().stream()
                    .filter(a -> a.getQuestion().getId().equals(answer.getQuestionId()))
                    .filter(a -> submittingTeam.equals(a.getSubmittingTeam()))
                    .findAny()
                    .isPresent();

            if (hasAnswered) {
                throw new IllegalArgumentException("This team has already submitted an answer for this question.");
            }
        }

        return answerReturnDTOService
                .answerToAnswerReturnDTO(
                        answerRepository
                                .save(answerInputDTOService.answerInputDTOToAnswer(answer, submittingTeam.getId())));
    }

}
