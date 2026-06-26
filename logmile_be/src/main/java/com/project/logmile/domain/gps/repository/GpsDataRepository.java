package com.project.logmile.domain.gps.repository;

import com.project.logmile.domain.gps.entity.GpsData;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GpsDataRepository extends JpaRepository<GpsData, Long> {

	List<GpsData> findByDriveLogIdOrderByRecordedAtAsc(Long driveLogId);

	// 일일 총 운행 시간 계산용: 운전자 기준 오늘 GPS 수신 건수
	@Query("SELECT COUNT(g) FROM GpsData g WHERE g.driveLog.driver.id = :driverId " +
		"AND g.recordedAt BETWEEN :start AND :end")
	long countByDriverAndDateRange(@Param("driverId") Long driverId,
		@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	// 야간 운행(22:00~06:00) GPS 수신 건수
	@Query("SELECT COUNT(g) FROM GpsData g WHERE g.driveLog.id = :driveLogId " +
		"AND (FUNCTION('HOUR', g.recordedAt) >= 22 OR FUNCTION('HOUR', g.recordedAt) < 6)")
	long countNightByDriveLogId(@Param("driveLogId") Long driveLogId);
}
