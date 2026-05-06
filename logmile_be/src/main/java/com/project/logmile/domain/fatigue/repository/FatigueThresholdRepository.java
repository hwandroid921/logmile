package com.project.logmile.domain.fatigue.repository;

import com.project.logmile.domain.fatigue.entity.FatigueThreshold;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FatigueThresholdRepository extends JpaRepository<FatigueThreshold, Long> {

	Optional<FatigueThreshold> findByThresholdKey(String thresholdKey);
}
