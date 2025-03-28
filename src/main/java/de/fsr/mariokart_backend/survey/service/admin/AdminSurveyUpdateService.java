package de.fsr.mariokart_backend.survey.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminSurveyUpdateService {
    private final QuestionRepository questionRepository;
    private final QuestionInputDTOService questionInputDTOService;
    private final QuestionReturnDTOService questionReturnDTOService;

    public QuestionReturnDTO updateQuestion(Long id, QuestionInputDTO question) throws EntityNotFoundException {
        Question questionToUpdate = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));
        Question updatedQuestion = questionInputDTOService.questionInputDTOToQuestion(question);
        if (updatedQuestion.getQuestionText() != null) {
            questionToUpdate.setQuestionText(updatedQuestion.getQuestionText());
        }
        if (updatedQuestion.getActive() != null) {
            questionToUpdate.setActive(updatedQuestion.getActive());
        }
        if (updatedQuestion.getVisible() != null) {
            questionToUpdate.setVisible(updatedQuestion.getVisible());
        }
        if (updatedQuestion.getLive() != null) {
            questionToUpdate.setLive(updatedQuestion.getLive());
        }
        if (updatedQuestion instanceof MultipleChoiceQuestion) {
            if (((MultipleChoiceQuestion) updatedQuestion).getOptions() != null) {
                ((MultipleChoiceQuestion) questionToUpdate)
                        .setOptions(((MultipleChoiceQuestion) updatedQuestion).getOptions());
            }
        } else if (updatedQuestion instanceof CheckboxQuestion) {
            if (((CheckboxQuestion) updatedQuestion).getOptions() != null) {
                ((CheckboxQuestion) questionToUpdate).setOptions(((CheckboxQuestion) updatedQuestion).getOptions());
            }
        } else if (updatedQuestion instanceof FreeTextQuestion) {
            // nothing to update
        } else if (updatedQuestion instanceof TeamQuestion) {
            if (((TeamQuestion) updatedQuestion).getFinalTeamsOnly() != null) {
                ((TeamQuestion) questionToUpdate)
                        .setFinalTeamsOnly(((TeamQuestion) updatedQuestion).getFinalTeamsOnly());
            }
            if (((TeamQuestion) updatedQuestion).getTeams() != null) {
                ((TeamQuestion) questionToUpdate).setTeams(((TeamQuestion) updatedQuestion).getTeams());
            }
        } else {
            throw new IllegalArgumentException("Question type not supported.");
        }
        return questionReturnDTOService.questionToQuestionReturnDTO(questionRepository.save(questionToUpdate));
    }
}
