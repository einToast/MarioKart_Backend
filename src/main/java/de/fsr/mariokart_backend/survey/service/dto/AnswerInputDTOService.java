package de.fsr.mariokart_backend.survey.service.dto;

import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.SurveyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnswerInputDTOService {

    public QuestionRepository questionRepository;

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
        } else {
            throw new IllegalArgumentException("Invalid answer type.");
        }
        answer.setQuestion(questionRepository.findById(answerInputDTO.getQuestionId()).orElseThrow( () -> new EntityNotFoundException("There is no question with this id.")));
        return answer;
    }
}
