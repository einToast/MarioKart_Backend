package de.fsr.mariokart_backend.survey.service.dto;

import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnswerReturnDTOService {
    public AnswerReturnDTO answerToAnswerReturnDTO(Answer answer) {
        AnswerReturnDTO answerReturnDTO = new AnswerReturnDTO();
        answerReturnDTO.setQuestionId(answer.getQuestion().getId());
        if (answer instanceof MultipleChoiceAnswer) {
            answerReturnDTO.setMultipleChoiceSelectedOption(((MultipleChoiceAnswer) answer).getSelectedOption());
            answerReturnDTO.setAnswerType(QuestionType.MULTIPLE_CHOICE.toString());
        } else if (answer instanceof CheckboxAnswer) {
            answerReturnDTO.setCheckboxSelectedOptions(((CheckboxAnswer) answer).getSelectedOptions());
            answerReturnDTO.setAnswerType(QuestionType.CHECKBOX.toString());
        } else if (answer instanceof FreeTextAnswer) {
            answerReturnDTO.setFreeTextAnswer(((FreeTextAnswer) answer).getTextAnswer());
            answerReturnDTO.setAnswerType(QuestionType.FREE_TEXT.toString());
        } else {
            throw new IllegalArgumentException("Invalid answer type.");
        }
        return answerReturnDTO;
    }
}
