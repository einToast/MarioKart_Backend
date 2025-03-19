package de.fsr.mariokart_backend.survey.service.pub;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicSurveyCreateService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerInputDTOService answerInputDTOService;
    private final AnswerReturnDTOService answerReturnDTOService;
    private final TeamRepository teamRepository;

    private static final int MAX_ANSWERS_PER_TEAM = 4;

    public AnswerReturnDTO submitAnswer(AnswerInputDTO answer, Long teamId) throws EntityNotFoundException {
        Question question = questionRepository.findById(answer.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));

        if (!question.getActive() || !question.getVisible()) {
            throw new IllegalStateException("Question is not active or visible.");
        }

        final Team submittingTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this id."));

        // Skip team answer limit check for free text questions
        if (!"FREE_TEXT".equals(answer.getAnswerType())) {
            long teamAnswerCount = answerRepository.findAll().stream()
                    .filter(a -> a.getQuestion().getId().equals(answer.getQuestionId()))
                    .filter(a -> submittingTeam.equals(a.getSubmittingTeam()))
                    .count();

            if (teamAnswerCount >= MAX_ANSWERS_PER_TEAM) {
                throw new IllegalArgumentException("Maximum number of answers per team reached.");
            }
        }

        return answerReturnDTOService
                .answerToAnswerReturnDTO(
                        answerRepository.save(answerInputDTOService.answerInputDTOToAnswer(answer, teamId)));
    }

}
