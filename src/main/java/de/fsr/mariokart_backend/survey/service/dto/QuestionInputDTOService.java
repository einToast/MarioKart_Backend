package de.fsr.mariokart_backend.survey.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class QuestionInputDTOService {

    public Question questionInputDTOToQuestion(QuestionInputDTO questionInputDTO) {
        Question question = null;
        if (questionInputDTO.getQuestionType().equals(QuestionType.MULTIPLE_CHOICE.toString())) {
            question = new MultipleChoiceQuestion();
            ((MultipleChoiceQuestion) question).setOptions(questionInputDTO.getOptions());
        } else if (questionInputDTO.getQuestionType().equals(QuestionType.CHECKBOX.toString())) {
            question = new CheckboxQuestion();
            ((CheckboxQuestion) question).setOptions(questionInputDTO.getOptions());
        } else if (questionInputDTO.getQuestionType().equals(QuestionType.FREE_TEXT.toString())) {
            question = new FreeTextQuestion();
        } else {
            throw new IllegalArgumentException("Invalid question type.");
        }
        question.setQuestionText(questionInputDTO.getQuestionText());
        question.setActive(questionInputDTO.isActive());
        question.setVisible(questionInputDTO.isVisible());
        question.setLive(questionInputDTO.isLive());
        return question;
    }
}
