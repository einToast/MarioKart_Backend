package de.fsr.mariokart_backend.survey.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.survey.model.SurveyKey;

public interface SurveyKeyRepository extends JpaRepository<SurveyKey, Long> {
    Optional<SurveyKey> findByToken(String token);
}
