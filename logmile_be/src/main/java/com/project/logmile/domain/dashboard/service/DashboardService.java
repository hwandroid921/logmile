package com.project.logmile.domain.dashboard.service;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.common.enums.DashboardActionType;
import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.dashboard.entity.DashboardAction;
import com.project.logmile.domain.dashboard.repository.DashboardActionRepository;
import com.project.logmile.domain.dashboard.dto.DashboardSummaryResponse;
import com.project.logmile.domain.dashboard.dto.VehicleStatusResponse;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final DriveLogRepository driveLogRepository;
	private final FatigueEventRepository fatigueEventRepository;
	private final DashboardActionRepository dashboardActionRepository;
	private final TenantAccessService tenantAccessService;

	/**
	 * 회사별 대시보드 요약 조회
	 */
	@Transactional(readOnly = true)
	public DashboardSummaryResponse getSummary(LocalDate date) {
		Long companyId = tenantAccessService.getCurrentCompanyId();
		LocalDate targetDate = resolveTargetDate(date);
		LocalDateTime start = targetDate.atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		boolean isToday = targetDate.equals(LocalDate.now());

		List<DriveLog> dashboardLogs = isToday
			? driveLogRepository.findByCompanyIdAndStatus(companyId, DriveLogStatus.RUNNING)
			: driveLogRepository.findByCompanyIdAndStartedAtBetweenOrderByStartedAtDesc(
				companyId, start, end);

		int runningCount = dashboardLogs.size();
		int cautionCount = 0;
		int dangerCount  = 0;

		for (DriveLog log : dashboardLogs) {
			FatigueLevel level = resolveFatigueLevel(log);
			if (level == FatigueLevel.DANGER)  dangerCount++;
			else if (level == FatigueLevel.CAUTION) cautionCount++;
		}

		long todayCompleted = driveLogRepository.countByCompanyIdAndStatusAndStartedAtBetween(
			companyId, DriveLogStatus.COMPLETED, start, end);

		Double avgScore = fatigueEventRepository.avgFatigueScoreByCompanyAndDateRange(
			companyId, start, end);
		double avg = avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0;

		return DashboardSummaryResponse.of(runningCount, cautionCount, dangerCount,
			(int) todayCompleted, avg);
	}

	/**
	 * 운행 중인 차량 목록 (차량별 현재 피로도 포함)
	 */
	@Transactional(readOnly = true)
	public List<VehicleStatusResponse> getRunningVehicles(LocalDate date) {
		Long companyId = tenantAccessService.getCurrentCompanyId();
		LocalDate targetDate = resolveTargetDate(date);
		LocalDateTime start = targetDate.atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		boolean isToday = targetDate.equals(LocalDate.now());

		List<DriveLog> dashboardLogs = isToday
			? driveLogRepository.findByCompanyIdAndStatus(companyId, DriveLogStatus.RUNNING)
			: driveLogRepository.findByCompanyIdAndStartedAtBetweenOrderByStartedAtDesc(
				companyId, start, end);

		Map<Long, Map<DashboardActionType, LocalDateTime>> actionMap = buildActionMap(dashboardLogs);
		Map<Long, Integer> restGuideCountMap = buildRestGuideCountMap(dashboardLogs);

		return dashboardLogs.stream()
			.sorted(Comparator.comparing(DriveLog::getStartedAt).reversed())
			.map(log -> {
			FatigueEvent latest = fatigueEventRepository
				.findTopByDriveLogIdOrderByOccurredAtDesc(log.getId())
				.orElse(null);

			Integer score = resolveFatigueScore(log, latest);
			FatigueLevel level = resolveFatigueLevel(log, latest);

			return VehicleStatusResponse.of(
				log.getId(),
				log.getVehicle().getId(),
				log.getVehicle().getPlateNo(),
				log.getVehicle().getType(),
				log.getDriver().getId(),
				log.getDriver().getName(),
				log.getDriver().getPhone(),
				score,
				level,
				log.getStatus(),
				restGuideCountMap.getOrDefault(log.getId(), 0),
				getActionTime(actionMap, log.getId(), DashboardActionType.REST_GUIDE),
				getActionTime(actionMap, log.getId(), DashboardActionType.PHONE_RECOMMENDATION),
				log.getStartedAt()
			);
		}).toList();
	}

	private LocalDate resolveTargetDate(LocalDate date) {
		return date != null ? date : LocalDate.now();
	}

	private Integer resolveFatigueScore(DriveLog log, FatigueEvent latest) {
		if (latest != null && latest.getFatigueScore() != null) {
			return latest.getFatigueScore();
		}
		return log.getMaxFatigueScore() != null ? log.getMaxFatigueScore() : 0;
	}

	private FatigueLevel resolveFatigueLevel(DriveLog log) {
		return resolveFatigueLevel(log, null);
	}

	private FatigueLevel resolveFatigueLevel(DriveLog log, FatigueEvent latest) {
		if (latest != null && latest.getFatigueLevel() != null) {
			return latest.getFatigueLevel();
		}
		return log.getMaxFatigueLevel() != null ? log.getMaxFatigueLevel() : FatigueLevel.NORMAL;
	}

	private Map<Long, Map<DashboardActionType, LocalDateTime>> buildActionMap(List<DriveLog> logs) {
		if (logs.isEmpty()) {
			return Map.of();
		}

		List<Long> driveLogIds = logs.stream().map(DriveLog::getId).toList();
		return dashboardActionRepository.findByDriveLogIds(driveLogIds).stream()
			.collect(java.util.stream.Collectors.groupingBy(
				action -> action.getDriveLog().getId(),
				java.util.stream.Collectors.toMap(
					DashboardAction::getActionType,
					DashboardAction::getCreatedAt,
					(createdAt1, createdAt2) -> createdAt1.isAfter(createdAt2) ? createdAt1 : createdAt2
				)
			));
	}

	private LocalDateTime getActionTime(Map<Long, Map<DashboardActionType, LocalDateTime>> actionMap,
		Long driveLogId, DashboardActionType actionType) {
		return actionMap.getOrDefault(driveLogId, Map.of()).get(actionType);
	}

	private Map<Long, Integer> buildRestGuideCountMap(List<DriveLog> logs) {
		if (logs.isEmpty()) return Map.of();
		List<Long> ids = logs.stream().map(DriveLog::getId).toList();
		return dashboardActionRepository.countRestGuideByDriveLogIds(ids).stream()
			.collect(java.util.stream.Collectors.toMap(
				row -> (Long) row[0],
				row -> ((Number) row[1]).intValue()
			));
	}
}
