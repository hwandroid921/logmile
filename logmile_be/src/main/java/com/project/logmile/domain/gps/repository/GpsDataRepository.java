package com.project.logmile.domain.gps.repository;

import com.project.logmile.domain.gps.entity.GpsData;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsDataRepository extends JpaRepository<GpsData, Long> {

	List<GpsData> findByDriveLogIdOrderByRecordedAtAsc(Long driveLogId);
}
