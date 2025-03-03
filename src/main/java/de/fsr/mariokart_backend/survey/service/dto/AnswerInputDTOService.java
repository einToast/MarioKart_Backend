package de.fsr.mariokart_backend.survey.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AnswerInputDTOService {

    private final QuestionRepository questionRepository;
    private final TeamRepository teamRepository;

    public Answer answerInputDTOToAnswer(AnswerInputDTO answerInputDTO) {
        Answer answer = null;
        if (answerInputDTO.getAnswerType().equals(QuestionType.MULTIPLE_CHOICE.toString())) {
            answer = new MultipleChoiceAnswer();
            ((MultipleChoiceAnswer) answer).setSelectedOption(answerInputDTO.getMultipleChoiceSelectedOption());
        } else if (answerInputDTO.getAnswerType().equals(QuestionType.CHECKBOX.toString())) {
            answer = new CheckboxAnswer();
            ((CheckboxAnswer) answer).setSelectedOptions(answerInputDTO.getCheckboxSelectedOptions());
        } else if (answerInputDTO.getAnswerType().equals(QuestionType.FREE_TEXT.toString())) {
            answer = new FreeTextAnswer();
            ((FreeTextAnswer) answer).setTextAnswer(answerInputDTO.getFreeTextAnswer());
        } else if (answerInputDTO.getAnswerType().equals(QuestionType.TEAM.toString())) {
            answer = new TeamAnswer();
            Question question = questionRepository.findById(answerInputDTO.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));
            if (question instanceof TeamQuestion) {
                Team team = teamRepository
                        .findByTeamName(((TeamQuestion) question).getTeams().get(answerInputDTO.getTeamSelectedOption())
                                .getTeamName())
                        .orElseThrow(() -> new EntityNotFoundException("There is no team with this id."));
                ((TeamAnswer) answer).setTeam(team);
            } else {
                throw new IllegalArgumentException("Invalid question type.");
            }
        } else {
            throw new IllegalArgumentException("Invalid answer type.");
        }
        answer.setQuestion(questionRepository.findById(answerInputDTO.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id.")));
        return answer;
    }
}
