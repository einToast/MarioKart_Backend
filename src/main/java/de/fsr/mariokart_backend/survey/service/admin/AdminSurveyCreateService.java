package de.fsr.mariokart_backend.survey.service.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "survey")
@CacheEvict(allEntries = true)
public class AdminSurveyCreateService {

    private final QuestionRepository questionRepository;
    private final QuestionInputDTOService questionInputDTOService;
    private final QuestionReturnDTOService questionReturnDTOService;
    private final WebSocketService webSocketService;

    public QuestionReturnDTO createQuestion(QuestionInputDTO survey) {
        QuestionReturnDTO question = questionReturnDTOService.questionToQuestionReturnDTO(
                questionRepository.save(questionInputDTOService.questionInputDTOToQuestion(survey)));
        webSocketService.sendMessage("/topic/questions", "create");
        return question;
    }

}
