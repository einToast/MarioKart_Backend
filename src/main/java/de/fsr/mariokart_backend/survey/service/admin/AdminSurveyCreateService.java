package de.fsr.mariokart_backend.survey.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminSurveyCreateService {

    private final QuestionRepository questionRepository;
    private final QuestionInputDTOService questionInputDTOService;
    private final QuestionReturnDTOService questionReturnDTOService;

    public QuestionReturnDTO createQuestion(QuestionInputDTO survey) {
        return questionReturnDTOService.questionToQuestionReturnDTO(
                questionRepository.save(questionInputDTOService.questionInputDTOToQuestion(survey)));
    }

}
