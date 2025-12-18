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
        if (answer instanceof MultipleChoiceAnswer choiceAnswer) {
            answerReturnDTO.setMultipleChoiceSelectedOption(choiceAnswer.getSelectedOption());
            answerReturnDTO.setAnswerType(QuestionType.MULTIPLE_CHOICE.toString());
        } else if (answer instanceof CheckboxAnswer checkboxAnswer) {
            answerReturnDTO.setCheckboxSelectedOptions(checkboxAnswer.getSelectedOptions());
            answerReturnDTO.setAnswerType(QuestionType.CHECKBOX.toString());
        } else if (answer instanceof FreeTextAnswer freeTextAnswer) {
            answerReturnDTO.setFreeTextAnswer(freeTextAnswer.getTextAnswer());
            answerReturnDTO.setAnswerType(QuestionType.FREE_TEXT.toString());
        } else if (answer instanceof TeamAnswer) {
            Question question = answer.getQuestion();
            if (question instanceof TeamQuestion teamQuestion) {
                answerReturnDTO.setTeamSelectedOption(
                        teamQuestion.getTeams().indexOf(((TeamAnswer) answer).getTeam()));
            } else {
                throw new IllegalArgumentException("Invalid question type.");
            }
            answerReturnDTO.setAnswerType(QuestionType.TEAM.toString());
        } else if (answer instanceof TeamOneFreeTextAnswer textAnswer) {
            answerReturnDTO.setFreeTextAnswer(
                    textAnswer.getTextAnswer());
            answerReturnDTO.setTeamSelectedOption(
                    textAnswer.getTeam().getId().intValue());
            answerReturnDTO.setAnswerType(QuestionType.TEAM_ONE_FREE_TEXT.toString());
        } else {
            throw new IllegalArgumentException("Invalid answer type.");
        }
        return answerReturnDTO;
    }
}
