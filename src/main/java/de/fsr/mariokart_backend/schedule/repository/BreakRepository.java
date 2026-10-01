package de.fsr.mariokart_backend.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.fsr.mariokart_backend.schedule.model.Break;

public interface BreakRepository extends JpaRepository<Break, Long> {

}
