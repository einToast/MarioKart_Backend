package de.fsr.mariokart_backend.survey.service.dto;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import lombok.AllArgsConstructor;

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
        } else if (question instanceof TeamQuestion) {
            questionReturnDTO.setOptions(((TeamQuestion) question).getTeams().stream()
                    .map(Team::getTeamName)
                    .collect(Collectors.toList()));
            questionReturnDTO.setFinalTeamsOnly(((TeamQuestion) question).getFinalTeamsOnly());
            questionReturnDTO.setQuestionType(QuestionType.TEAM.toString());
        } else if (question instanceof TeamOneFreeTextQuestion) {
            questionReturnDTO.setQuestionType(QuestionType.TEAM_ONE_FREE_TEXT.toString());
        } else {
            throw new IllegalArgumentException("Invalid question type.");
        }
        questionReturnDTO.setQuestionText(question.getQuestionText());
        questionReturnDTO.setActive(question.getActive());
        questionReturnDTO.setVisible(question.getVisible());
        questionReturnDTO.setLive(question.getLive());
        return questionReturnDTO;
    }
}
