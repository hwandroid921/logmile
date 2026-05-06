package com.project.logmile.domain.fatigue.repository;

import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FatigueEventRepository extends JpaRepository<FatigueEvent, Long> {

	List<FatigueEvent> findByDriveLogIdOrderByOccurredAtAsc(Long driveLogId);

	Optional<FatigueEvent> findTopByDriveLogIdOrderByOccurredAtDesc(Long driveLogId);

	// 운행중인 driveLog들의 가장 최근 피로도 이벤트 조회 (대시보드용)
	@Query("SELECT f FROM FatigueEvent f WHERE f.driveLog.id = :driveLogId " +
		"ORDER BY f.occurredAt DESC LIMIT 1")
	Optional<FatigueEvent> findLatestByDriveLogId(@Param("driveLogId") Long driveLogId);

	// 특정 회사의 오늘 완료 운행에 대한 평균 피로도 점수
	@Query("SELECT COALESCE(AVG(f.fatigueScore), 0) FROM FatigueEvent f " +
		"WHERE f.driveLog.company.id = :companyId " +
		"AND f.occurredAt BETWEEN :start AND :end")
	Double avgFatigueScoreByCompanyAndDateRange(
		@Param("companyId") Long companyId,
		@Param("start") java.time.LocalDateTime start,
		@Param("end") java.time.LocalDateTime end);
}
