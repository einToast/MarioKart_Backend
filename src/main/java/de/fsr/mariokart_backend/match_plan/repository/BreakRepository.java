package de.fsr.mariokart_backend.match_plan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fsr.mariokart_backend.match_plan.model.Break;

@Repository
public interface BreakRepository extends JpaRepository<Break, Long> {

}
