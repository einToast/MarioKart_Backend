package de.fsr.mariokart_backend.survey.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AnswerReturnDTOService {
    public AnswerReturnDTO answerToAnswerReturnDTO(Answer answer) {
        AnswerReturnDTO answerReturnDTO = new AnswerReturnDTO();
        answerReturnDTO.setQuestionId(answer.getQuestion().getId());
        switch (answer) {
            case MultipleChoiceAnswer choiceAnswer -> {
                answerReturnDTO.setMultipleChoiceSelectedOption(choiceAnswer.getSelectedOption());
                answerReturnDTO.setAnswerType(QuestionType.MULTIPLE_CHOICE.toString());
            }
            case CheckboxAnswer checkboxAnswer -> {
                answerReturnDTO.setCheckboxSelectedOptions(checkboxAnswer.getSelectedOptions());
                answerReturnDTO.setAnswerType(QuestionType.CHECKBOX.toString());
            }
            case FreeTextAnswer freeTextAnswer -> {
                answerReturnDTO.setFreeTextAnswer(freeTextAnswer.getTextAnswer());
                answerReturnDTO.setAnswerType(QuestionType.FREE_TEXT.toString());
            }
            case TeamAnswer teamAnswer -> {
                Question question = answer.getQuestion();
                if (question instanceof TeamQuestion teamQuestion) {
                    answerReturnDTO.setTeamSelectedOption(
                            teamQuestion.getTeams().indexOf(teamAnswer.getTeam()));
                } else {
                    throw new IllegalArgumentException("Invalid question type.");
                }
                answerReturnDTO.setAnswerType(QuestionType.TEAM.toString());
            }
            case TeamOneFreeTextAnswer freetextAnswer -> {
                answerReturnDTO.setFreeTextAnswer(
                        freetextAnswer.getTextAnswer());
                answerReturnDTO.setTeamSelectedOption(
                        freetextAnswer.getTeam().getId().intValue());
                answerReturnDTO.setAnswerType(QuestionType.TEAM_ONE_FREE_TEXT.toString());
            }
            default -> throw new IllegalArgumentException("Invalid answer type.");
        }
        return answerReturnDTO;
    }
}
