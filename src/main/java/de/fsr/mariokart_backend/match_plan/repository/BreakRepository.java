package de.fsr.mariokart_backend.match_plan.repository;

import de.fsr.mariokart_backend.match_plan.model.Break;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BreakRepository extends JpaRepository<Break, Long> {

}
