package com.project.logmile.domain.dashboard.service;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.dashboard.dto.DashboardSummaryResponse;
import com.project.logmile.domain.dashboard.dto.VehicleStatusResponse;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final DriveLogRepository driveLogRepository;
	private final FatigueEventRepository fatigueEventRepository;
	private final TenantAccessService tenantAccessService;

	/**
	 * 회사별 대시보드 요약 조회
	 */
	@Transactional(readOnly = true)
	public DashboardSummaryResponse getSummary() {
		Long companyId = tenantAccessService.getCurrentCompanyId();

		// 현재 RUNNING 상태인 운행 목록
		List<DriveLog> runningLogs = driveLogRepository.findByCompanyIdAndStatus(
			companyId, DriveLogStatus.RUNNING);

		int runningCount = runningLogs.size();
		int cautionCount = 0;
		int dangerCount  = 0;

		for (DriveLog log : runningLogs) {
			FatigueLevel level = log.getMaxFatigueLevel();
			if (level == FatigueLevel.DANGER)  dangerCount++;
			else if (level == FatigueLevel.CAUTION) cautionCount++;
		}

		// 오늘 완료된 운행 수
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		LocalDateTime now        = LocalDateTime.now();
		long todayCompleted = driveLogRepository.countByCompanyIdAndStatusAndStartedAtBetween(
			companyId, DriveLogStatus.COMPLETED, todayStart, now);

		// 오늘 평균 피로도 점수
		Double avgScore = fatigueEventRepository.avgFatigueScoreByCompanyAndDateRange(
			companyId, todayStart, now);
		double avg = avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0;

		return DashboardSummaryResponse.of(runningCount, cautionCount, dangerCount,
			(int) todayCompleted, avg);
	}

	/**
	 * 운행 중인 차량 목록 (차량별 현재 피로도 포함)
	 */
	@Transactional(readOnly = true)
	public List<VehicleStatusResponse> getRunningVehicles() {
		Long companyId = tenantAccessService.getCurrentCompanyId();

		List<DriveLog> runningLogs = driveLogRepository.findByCompanyIdAndStatus(
			companyId, DriveLogStatus.RUNNING);

		return runningLogs.stream().map(log -> {
			// 가장 최근 피로도 이벤트 조회
			FatigueEvent latest = fatigueEventRepository
				.findTopByDriveLogIdOrderByOccurredAtDesc(log.getId())
				.orElse(null);

			Integer score = latest != null ? latest.getFatigueScore() : 0;
			FatigueLevel level = latest != null ? latest.getFatigueLevel() : FatigueLevel.NORMAL;

			return VehicleStatusResponse.of(
				log.getId(),
				log.getVehicle().getId(),
				log.getVehicle().getPlateNo(),
				log.getVehicle().getType(),
				log.getDriver().getId(),
				log.getDriver().getName(),
				score,
				level,
				log.getStartedAt()
			);
		}).toList();
	}
}
