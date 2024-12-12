package de.fsr.mariokart_backend.survey.repository;

import de.fsr.mariokart_backend.survey.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByVisible(boolean visible);
}