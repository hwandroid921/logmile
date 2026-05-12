package com.project.logmile.domain.fatigue.service;

import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.fatigue.dto.FatigueStatsResponse;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FatigueStatsService {

	private static final int DEFAULT_DAYS = 14;
	private static final int MAX_DAYS = 30;

	private final TenantAccessService tenantAccessService;
	private final DriveLogRepository driveLogRepository;
	private final FatigueEventRepository fatigueEventRepository;

	@Transactional(readOnly = true)
	public List<FatigueStatsResponse> getDailyStats(LocalDate from, LocalDate to, Integer days) {
		LocalDate endDate = to != null ? to : LocalDate.now();
		LocalDate startDate = from != null ? from : endDate.minusDays(resolveDays(days) - 1L);

		Long companyId = tenantAccessService.getCurrentCompanyId();
		LocalDateTime start = startDate.atStartOfDay();
		LocalDateTime end = endDate.atTime(LocalTime.MAX);

		List<DriveLog> driveLogs = driveLogRepository
			.findByCompanyIdAndStartedAtBetweenOrderByStartedAtAsc(companyId, start, end);
		List<FatigueEvent> fatigueEvents = fatigueEventRepository
			.findByDriveLogCompanyIdAndOccurredAtBetweenOrderByOccurredAtAsc(companyId, start, end);

		Map<LocalDate, DailyAccumulator> stats = new LinkedHashMap<>();
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			stats.put(date, new DailyAccumulator());
		}

		for (DriveLog log : driveLogs) {
			DailyAccumulator acc = stats.get(log.getStartedAt().toLocalDate());
			if (acc == null) {
				continue;
			}
			acc.driveLogCount++;
			acc.totalDrivingMinutes += valueOrZero(log.getTotalDrivingMinutes());
			acc.nightDrivingMinutes += valueOrZero(log.getNightDrivingMinutes());
		}

		for (FatigueEvent event : fatigueEvents) {
			DailyAccumulator acc = stats.get(event.getOccurredAt().toLocalDate());
			if (acc == null) {
				continue;
			}
			acc.scoreSum += valueOrZero(event.getFatigueScore());
			acc.scoreCount++;
			acc.restViolationCount += valueOrZero(event.getRestViolationCount());
			if (event.getFatigueLevel() == FatigueLevel.DANGER) {
				acc.dangerEventCount++;
			}
		}

		return stats.entrySet()
			.stream()
			.map(entry -> entry.getValue().toResponse(entry.getKey()))
			.toList();
	}

	private int resolveDays(Integer days) {
		if (days == null || days <= 0) {
			return DEFAULT_DAYS;
		}
		return Math.min(days, MAX_DAYS);
	}

	private int valueOrZero(Integer value) {
		return value != null ? value : 0;
	}

	private static class DailyAccumulator {

		private long driveLogCount;
		private long totalDrivingMinutes;
		private long nightDrivingMinutes;
		private long restViolationCount;
		private long dangerEventCount;
		private long scoreSum;
		private long scoreCount;

		private FatigueStatsResponse toResponse(LocalDate date) {
			double averageScore = scoreCount > 0
				? Math.round((scoreSum / (double) scoreCount) * 10.0) / 10.0
				: 0.0;

			return new FatigueStatsResponse(
				date,
				driveLogCount,
				totalDrivingMinutes,
				nightDrivingMinutes,
				restViolationCount,
				dangerEventCount,
				averageScore
			);
		}
	}
}
