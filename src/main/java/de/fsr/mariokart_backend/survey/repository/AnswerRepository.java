package de.fsr.mariokart_backend.survey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.survey.model.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findAllByQuestionId(Long id);

    void deleteAllByQuestionId(Long id);

    long countByQuestionIdAndSubmittingTeamId(Long questionId, Long submittingTeamId);

    boolean existsByQuestionIdAndSubmittingTeamId(Long questionId, Long submittingTeamId);

    boolean existsByQuestionIdAndSurveyKeyId(Long questionId, Long surveyKeyId);
}
