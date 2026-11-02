package de.fsr.mariokart_backend.survey.service.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminSurveyReadService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionReturnDTOService questionReturnDTOService;
    private final AnswerReturnDTOService answerReturnDTOService;

    public List<QuestionReturnDTO> getQuestions() {
        return questionRepository.findAll().stream()
                .map(questionReturnDTOService::questionToQuestionReturnDTO)
                .toList();
    }

    public List<AnswerReturnDTO> getAnswersOfQuestion(Long id) throws EntityNotFoundException {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));
        if (!(question instanceof FreeTextQuestion) && !(question instanceof TeamOneFreeTextQuestion)) {
            throw new IllegalArgumentException("Question is not a FreeTextQuestion or TeamOneFreeTextQuestion");
        }
        return answerRepository.findAllByQuestionId(id).stream()
                .map(answerReturnDTOService::answerToAnswerReturnDTO)
                .toList();
    }

    public List<Integer> getStatisticsOfQuestion(Long id) throws EntityNotFoundException {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        List<Answer> answers = answerRepository.findAllByQuestionId(id);

        return switch (question) {
            case MultipleChoiceQuestion mcQuestion -> countSelections(mcQuestion.getOptions().size(),
                    answers.stream()
                            .filter(MultipleChoiceAnswer.class::isInstance)
                            .map(answer -> ((MultipleChoiceAnswer) answer).getSelectedOption()));
            case CheckboxQuestion cbQuestion -> countSelections(cbQuestion.getOptions().size(),
                    answers.stream()
                            .filter(CheckboxAnswer.class::isInstance)
                            .map(answer -> ((CheckboxAnswer) answer).getSelectedOptions())
                            .filter(Objects::nonNull)
                            .flatMap(selectedOptions -> selectedOptions.stream().distinct()));
            case TeamQuestion teamQuestion -> countSelections(teamQuestion.getTeams().size(),
                    answers.stream()
                            .filter(TeamAnswer.class::isInstance)
                            .map(answer -> teamQuestion.getTeams().indexOf(((TeamAnswer) answer).getTeam())));
            default -> throw new IllegalArgumentException("QuestionType not supported");
        };
    }

    private List<Integer> countSelections(int optionCount, Stream<Integer> selectedIndices) {
        List<Integer> statistics = new ArrayList<>(Collections.nCopies(optionCount, 0));

        selectedIndices
                .filter(index -> index != null && index >= 0 && index < optionCount)
                .forEach(index -> statistics.set(index, statistics.get(index) + 1));

        return statistics;
    }

    public Integer getNumberOfAnswers(Long id) {
        return answerRepository.findAllByQuestionId(id).size();
    }
}
