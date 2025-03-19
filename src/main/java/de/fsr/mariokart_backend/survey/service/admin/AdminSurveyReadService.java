package de.fsr.mariokart_backend.survey.service.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

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
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
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
        if (!(question instanceof FreeTextQuestion)) {
            throw new IllegalArgumentException("Question is not a FreeTextQuestion");
        }
        return answerRepository.findAllByQuestionId(id).stream()
                .map(answerReturnDTOService::answerToAnswerReturnDTO)
                .toList();
    }

    public List<Integer> getStatisticsOfQuestion(Long id) throws EntityNotFoundException {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        List<Answer> answers = answerRepository.findAllByQuestionId(id);
        final List<Integer> statistics;

        if (question instanceof MultipleChoiceQuestion) {
            MultipleChoiceQuestion mcQuestion = (MultipleChoiceQuestion) question;
            int optionCount = mcQuestion.getOptions().size();
            statistics = new ArrayList<>(Collections.nCopies(optionCount, 0));

            answers.stream()
                    .map(answer -> (MultipleChoiceAnswer) answer)
                    .forEach(answer -> {
                        int selectedOption = answer.getSelectedOption();
                        if (selectedOption >= 0 && selectedOption < optionCount) {
                            statistics.set(selectedOption, statistics.get(selectedOption) + 1);
                        }
                    });

        } else if (question instanceof CheckboxQuestion) {
            CheckboxQuestion cbQuestion = (CheckboxQuestion) question;
            int optionCount = cbQuestion.getOptions().size();
            statistics = new ArrayList<>(Collections.nCopies(optionCount, 0));

            answers.stream()
                    .map(answer -> (CheckboxAnswer) answer)
                    .forEach(answer -> {
                        answer.getSelectedOptions().forEach(option -> {
                            if (option >= 0 && option < optionCount) {
                                statistics.set(option, statistics.get(option) + 1);
                            }
                        });
                    });

        } else if (question instanceof TeamQuestion) {
            TeamQuestion teamQuestion = (TeamQuestion) question;
            int teamCount = teamQuestion.getTeams().size();
            statistics = new ArrayList<>(Collections.nCopies(teamCount, 0));

            answers.stream()
                    .map(answer -> (TeamAnswer) answer)
                    .forEach(answer -> {
                        int teamIndex = teamQuestion.getTeams().indexOf(answer.getTeam());
                        if (teamIndex >= 0) {
                            statistics.set(teamIndex, statistics.get(teamIndex) + 1);
                        }
                    });

        } else {
            throw new IllegalArgumentException("QuestionType not supported");
        }

        return statistics;
    }

    public Integer getNumberOfAnswers(Long id) {
        return answerRepository.findAllByQuestionId(id).size();
    }
}
