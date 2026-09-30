package de.fsr.mariokart_backend.survey.service.pub;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.SurveyKeyRequiredException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.settings.model.SurveyKeyMode;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.SurveyKey;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerSubmissionResult;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.repository.SurveyKeyRepository;
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
    private final TournamentRepository tournamentRepository;
    private final SurveyKeyRepository surveyKeyRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_ANSWERS_PER_TEAM = 4;

    @Transactional(rollbackFor = Exception.class)
    public AnswerSubmissionResult submitAnswer(AnswerInputDTO answer, String userJson, String surveyKeyToken)
            throws EntityNotFoundException, JacksonException, SurveyKeyRequiredException {

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
            long teamAnswerCount = answerRepository.countByQuestionIdAndSubmittingTeamId(answer.getQuestionId(),
                    submittingTeam.getId());

            if (teamAnswerCount >= MAX_ANSWERS_PER_TEAM) {
                throw new IllegalArgumentException("Maximum number of answers per team reached.");
            }
        }

        if ("TEAM_ONE_FREE_TEXT".equals(answer.getAnswerType())) {
            if (answerRepository.existsByQuestionIdAndSubmittingTeamId(answer.getQuestionId(), submittingTeam.getId())) {
                throw new IllegalArgumentException("This team has already submitted an answer for this question.");
            }
        }

        Tournament settings = tournamentRepository.findAll().stream().findFirst().orElseGet(Tournament::new);
        SurveyKey surveyKey = resolveSurveyKey(settings.getSurveyKeyMode(), surveyKeyToken);

        if (surveyKey != null && question.isOneAnswerPerKey()
                && answerRepository.existsByQuestionIdAndSurveyKeyId(answer.getQuestionId(), surveyKey.getId())) {
            throw new IllegalArgumentException("This survey key has already been used to answer this question.");
        }

        // Map before issuing a key so that invalid answers never hand out a key
        Answer answerToSave = answerInputDTOService.answerInputDTOToAnswer(answer, submittingTeam.getId());

        String issuedSurveyKey = null;
        if (surveyKey == null && settings.getSurveyKeyMode() == SurveyKeyMode.DISTRIBUTING) {
            surveyKey = surveyKeyRepository.save(new SurveyKey(null, UUID.randomUUID().toString()));
            issuedSurveyKey = surveyKey.getToken();
        }
        answerToSave.setSurveyKey(surveyKey);

        return new AnswerSubmissionResult(
                answerReturnDTOService.answerToAnswerReturnDTO(answerRepository.save(answerToSave)),
                issuedSurveyKey);
    }

    private SurveyKey resolveSurveyKey(SurveyKeyMode mode, String surveyKeyToken) throws SurveyKeyRequiredException {
        if (mode == SurveyKeyMode.DISABLED) {
            return null;
        }

        SurveyKey surveyKey = null;
        if (surveyKeyToken != null && !surveyKeyToken.isBlank()) {
            surveyKey = surveyKeyRepository.findByToken(surveyKeyToken).orElse(null);
        }

        if (surveyKey == null && mode == SurveyKeyMode.REQUIRED) {
            throw new SurveyKeyRequiredException("A valid survey key is required to answer this question.");
        }
        return surveyKey;
    }

}
