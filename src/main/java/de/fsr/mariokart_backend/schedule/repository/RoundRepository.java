package de.fsr.mariokart_backend.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.schedule.model.Round;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByFinalGameTrue();

    List<Round> findByStartTimeAfter(LocalDateTime startTime);

    List<Round> findByStartTimeBefore(LocalDateTime startTime);

    List<Round> findByPlayedFalse();

    Optional<Round> findByRoundNumber(int roundNumber);

    Integer countByPlayedFalse();

    @Transactional
    void deleteAllByFinalGameTrue();
}