package de.fsr.mariokart_backend.survey.repository;

import de.fsr.mariokart_backend.survey.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findAllByQuestionId(Long id);

    void deleteAllByQuestionId(Long id);
}

