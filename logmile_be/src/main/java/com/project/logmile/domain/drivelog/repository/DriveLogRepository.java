package com.project.logmile.domain.drivelog.repository;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriveLogRepository extends JpaRepository<DriveLog, Long> {

	List<DriveLog> findByCompanyIdOrderByStartedAtDesc(Long companyId);

	List<DriveLog> findByStatus(DriveLogStatus status);

	List<DriveLog> findByCompanyIdAndStatus(Long companyId, DriveLogStatus status);

	long countByCompanyIdAndStatusAndStartedAtBetween(Long companyId, DriveLogStatus status,
		java.time.LocalDateTime start, java.time.LocalDateTime end);

	List<DriveLog> findByCompanyIdAndStartedAtBetweenOrderByStartedAtAsc(Long companyId,
		LocalDateTime start, LocalDateTime end);
}
