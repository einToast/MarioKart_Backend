package de.fsr.mariokart_backend.survey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fsr.mariokart_backend.survey.model.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByVisible(boolean visible);
}