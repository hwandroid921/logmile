package com.project.logmile.domain.fatigue.repository;

import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FatigueEventRepository extends JpaRepository<FatigueEvent, Long> {

	List<FatigueEvent> findByDriveLogIdOrderByOccurredAtAsc(Long driveLogId);

	Optional<FatigueEvent> findTopByDriveLogIdOrderByOccurredAtDesc(Long driveLogId);
}
