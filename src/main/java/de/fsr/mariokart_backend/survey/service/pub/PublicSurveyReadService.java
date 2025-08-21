package de.fsr.mariokart_backend.survey.service.pub;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyReadService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "survey")
public class PublicSurveyReadService {
    private final QuestionRepository questionRepository;
    private final QuestionReturnDTOService questionReturnDTOService;
    private final AdminSurveyReadService adminSurveyReadService;

    @Cacheable(key = "'visibleQuestions'", sync = true)
    public List<QuestionReturnDTO> getVisibleQuestions() {
        return questionRepository.findAllByVisible(true).stream()
                .map(questionReturnDTOService::questionToQuestionReturnDTO)
                .toList();
    }

    @Cacheable(key = "'questionStatistics_' + #id", sync = true)
    public List<Integer> getStatisticsOfQuestion(Long id) throws EntityNotFoundException {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        if ((!question.getVisible()) || question.getActive()) {
            throw new IllegalStateException("Question is not visible or is active");
        }
        return adminSurveyReadService.getStatisticsOfQuestion(id);

    }
}
