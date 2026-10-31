package de.fsr.mariokart_backend.schedule.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.schedule.model.Break;

public interface BreakRepository extends JpaRepository<Break, Long> {

    Optional<Break> findFirstByOrderByIdAsc();
}
