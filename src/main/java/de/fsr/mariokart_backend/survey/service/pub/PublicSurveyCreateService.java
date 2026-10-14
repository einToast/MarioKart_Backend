package de.fsr.mariokart_backend.survey.service.pub;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.AnswerLimitReachedException;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.SurveyKeyRequiredException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.settings.model.SurveyKeyMode;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.SurveyKey;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerSubmissionResult;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
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
            throws EntityNotFoundException, JacksonException, SurveyKeyRequiredException, AnswerLimitReachedException {

        if (userJson == null || userJson.isEmpty()) {
            throw new IllegalArgumentException("User JSON is null or empty.");
        }

        Map<String, Object> userMap = objectMapper.readValue(userJson, new TypeReference<Map<String, Object>>() {
        });
        if (!(userMap.get("teamId") instanceof Number teamId)) {
            throw new IllegalArgumentException("User JSON has no team.");
        }

        Question question = questionRepository.findById(answer.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));

        if (!question.getActive() || !question.getVisible()) {
            throw new IllegalStateException("Question is not active or visible.");
        }

        QuestionType questionType = validateAnswer(question, answer);

        final Team submittingTeam = teamRepository.findById(teamId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this id."));

        // Skip team answer limit check for free text questions
        if (questionType != QuestionType.FREE_TEXT) {
            long teamAnswerCount = answerRepository.countByQuestionIdAndSubmittingTeamId(answer.getQuestionId(),
                    submittingTeam.getId());

            if (teamAnswerCount >= MAX_ANSWERS_PER_TEAM) {
                throw new AnswerLimitReachedException("Maximum number of answers per team reached.");
            }
        }

        if (questionType == QuestionType.TEAM_ONE_FREE_TEXT
                && answerRepository.existsByQuestionIdAndSubmittingTeamId(answer.getQuestionId(), submittingTeam.getId())) {
            throw new AnswerLimitReachedException("This team has already submitted an answer for this question.");
        }

        Tournament settings = tournamentRepository.findAll().stream().findFirst().orElseGet(Tournament::new);
        SurveyKey surveyKey = resolveSurveyKey(settings.getSurveyKeyMode(), surveyKeyToken);

        if (surveyKey != null && question.isOneAnswerPerKey()
                && answerRepository.existsByQuestionIdAndSurveyKeyId(answer.getQuestionId(), surveyKey.getId())) {
            throw new AnswerLimitReachedException("This survey key has already been used to answer this question.");
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

    private QuestionType validateAnswer(Question question, AnswerInputDTO answer) {
        QuestionType questionType = switch (question) {
            case MultipleChoiceQuestion _ -> QuestionType.MULTIPLE_CHOICE;
            case CheckboxQuestion _ -> QuestionType.CHECKBOX;
            case FreeTextQuestion _ -> QuestionType.FREE_TEXT;
            case TeamQuestion _ -> QuestionType.TEAM;
            case TeamOneFreeTextQuestion _ -> QuestionType.TEAM_ONE_FREE_TEXT;
            default -> throw new IllegalArgumentException("Invalid answer: unknown question type.");
        };
        if (!questionType.toString().equals(answer.getAnswerType())) {
            throw new IllegalArgumentException("Invalid answer: the answer type does not match the question.");
        }

        boolean valid = switch (question) {
            case MultipleChoiceQuestion choiceQuestion ->
                isOption(answer.getMultipleChoiceSelectedOption(), choiceQuestion.getOptions());
            case CheckboxQuestion checkboxQuestion -> {
                List<Integer> selected = answer.getCheckboxSelectedOptions();
                yield selected != null && !selected.isEmpty()
                        && selected.stream().allMatch(option -> isOption(option, checkboxQuestion.getOptions()))
                        && new HashSet<>(selected).size() == selected.size();
            }
            case TeamQuestion teamQuestion -> isOption(answer.getTeamSelectedOption(), teamQuestion.getTeams());
            case TeamOneFreeTextQuestion _ -> hasText(answer.getFreeTextAnswer())
                    && answer.getTeamSelectedOption() != null;
            default -> hasText(answer.getFreeTextAnswer());
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid answer: no valid option selected.");
        }
        return questionType;
    }

    private static boolean isOption(Integer index, List<?> options) {
        return index != null && options != null && index >= 0 && index < options.size();
    }

    private static boolean hasText(String text) {
        return text != null && !text.isBlank();
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
