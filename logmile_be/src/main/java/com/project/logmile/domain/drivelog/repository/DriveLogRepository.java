package com.project.logmile.domain.drivelog.repository;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriveLogRepository extends JpaRepository<DriveLog, Long> {

	List<DriveLog> findByCompanyIdOrderByStartedAtDesc(Long companyId);

	List<DriveLog> findByStatus(DriveLogStatus status);
}
