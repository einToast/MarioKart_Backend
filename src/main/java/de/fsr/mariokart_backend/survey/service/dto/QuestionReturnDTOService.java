package de.fsr.mariokart_backend.survey.service.dto;

import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QuestionReturnDTOService {
    public QuestionReturnDTO questionToQuestionReturnDTO(Question question) {
        QuestionReturnDTO questionReturnDTO = new QuestionReturnDTO();
        questionReturnDTO.setId(question.getId());
        if (question instanceof MultipleChoiceQuestion) {
            questionReturnDTO.setOptions(((MultipleChoiceQuestion) question).getOptions());
            questionReturnDTO.setQuestionType(QuestionType.MULTIPLE_CHOICE.toString());
        } else if (question instanceof CheckboxQuestion) {
            questionReturnDTO.setOptions(((CheckboxQuestion) question).getOptions());
            questionReturnDTO.setQuestionType(QuestionType.CHECKBOX.toString());
        } else if (question instanceof FreeTextQuestion) {
            questionReturnDTO.setQuestionType(QuestionType.FREE_TEXT.toString());
        } else {
            throw new IllegalArgumentException("Invalid question type.");
        }
        questionReturnDTO.setQuestionText(question.getQuestionText());
        questionReturnDTO.setActive(question.getActive());

        return questionReturnDTO;
    }
}
