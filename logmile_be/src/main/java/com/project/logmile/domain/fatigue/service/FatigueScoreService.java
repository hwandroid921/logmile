package com.project.logmile.domain.fatigue.service;

import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.enums.RestType;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import com.project.logmile.domain.gps.repository.GpsDataRepository;
import com.project.logmile.domain.rest.entity.RestEvent;
import com.project.logmile.domain.rest.repository.RestEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FatigueScoreService {

	// GPS 수신 간격 (분) — 시뮬레이터 기본값
	private static final int GPS_INTERVAL_MINUTES = 1;

	private final FatigueEventRepository fatigueEventRepository;
	private final RestEventRepository restEventRepository;
	private final GpsDataRepository gpsDataRepository;

	/**
	 * GPS 수신마다 호출. 피로도 점수를 계산하고 FatigueEvent를 저장한다.
	 */
	@Transactional
	public FatigueEvent calculate(DriveLog driveLog, LocalDateTime recordedAt) {
		Long driveLogId = driveLog.getId();
		Long driverId   = driveLog.getDriver().getId();

		// 1. 연속 운행 시간 (분)
		int continuousMinutes = calcContinuousDriving(driveLogId, driveLog.getStartedAt(), recordedAt);

		// 2. 일일 총 운행 시간 (분)
		int dailyMinutes = calcDailyDriving(driverId, recordedAt);

		// 3. 야간 운행 시간 (분)
		int nightMinutes = calcNightDriving(driveLogId);

		// 4. 휴식 횟수 및 위반 횟수
		List<RestType> validTypes = List.of(RestType.VALID, RestType.SUFFICIENT);
		int restCount     = (int) restEventRepository.countByDriveLogIdAndRestTypeIn(driveLogId, validTypes);
		int violationCount = calcRestViolation(driveLogId, driveLog.getStartedAt(), recordedAt, restCount);

		// 5. 최근 휴식 타입 (보정용)
		Optional<RestEvent> lastRest = restEventRepository
			.findTopByDriveLogIdAndRestTypeInOrderByRestEndedAtDesc(driveLogId, validTypes);

		// 6. 점수 계산
		int score = 0;
		score += continuousScore(continuousMinutes);
		score += dailyScore(dailyMinutes);
		score += nightScore(nightMinutes);
		score += violationScore(violationCount);
		score -= correctionScore(lastRest.map(RestEvent::getRestType).orElse(null));
		score = Math.max(0, score);

		// 7. 등급
		FatigueLevel level = toLevel(score);

		// 8. 근거 문자열
		String reason = buildReason(continuousMinutes, dailyMinutes, nightMinutes,
			violationCount, lastRest.map(RestEvent::getRestType).orElse(null));

		// 9. FatigueEvent 저장
		FatigueEvent event = FatigueEvent.create(driveLog, score, level,
			continuousMinutes, dailyMinutes, nightMinutes,
			restCount, violationCount, reason, recordedAt);

		FatigueEvent saved = fatigueEventRepository.save(event);

		// 10. DriveLog 최대 피로 업데이트
		driveLog.updateMaxFatigue(score, level);

		return saved;
	}

	@Transactional
	public FatigueEvent calculateManual(DriveLog driveLog,
		int continuousMinutes, int dailyMinutes, int nightMinutes,
		int restCount, int violationCount, String manualReason,
		LocalDateTime occurredAt) {
		occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
		Optional<RestEvent> lastRest = restEventRepository
			.findTopByDriveLogIdAndRestTypeInOrderByRestEndedAtDesc(
				driveLog.getId(), List.of(RestType.VALID, RestType.SUFFICIENT));

		int score = 0;
		score += continuousScore(continuousMinutes);
		score += dailyScore(dailyMinutes);
		score += nightScore(nightMinutes);
		score += violationScore(violationCount);
		score -= correctionScore(lastRest.map(RestEvent::getRestType).orElse(null));
		score = Math.max(0, score);

		FatigueLevel level = toLevel(score);
		String reason = manualReason != null && !manualReason.isBlank()
			? manualReason
			: buildReason(continuousMinutes, dailyMinutes, nightMinutes,
				violationCount, lastRest.map(RestEvent::getRestType).orElse(null));

		FatigueEvent event = FatigueEvent.create(driveLog, score, level,
			continuousMinutes, dailyMinutes, nightMinutes,
			restCount, violationCount, reason, occurredAt);
		FatigueEvent saved = fatigueEventRepository.save(event);
		driveLog.updateMaxFatigue(score, level);
		return saved;
	}

	// ── 연속 운행 계산 ─────────────────────────────────────────────
	private int calcContinuousDriving(Long driveLogId, LocalDateTime startedAt,
		LocalDateTime now) {
		Optional<RestEvent> lastValidRest = restEventRepository
			.findTopByDriveLogIdAndRestTypeInOrderByRestEndedAtDesc(
				driveLogId, List.of(RestType.VALID, RestType.SUFFICIENT));

		LocalDateTime base = lastValidRest.map(RestEvent::getRestEndedAt).orElse(startedAt);
		return (int) java.time.Duration.between(base, now).toMinutes();
	}

	private int continuousScore(int minutes) {
		if (minutes >= 240) return 65;
		if (minutes >= 180) return 45;
		if (minutes >= 120) return 25;
		if (minutes >=  90) return 10;
		return 0;
	}

	// ── 일일 운행 계산 ──────────────────────────────────────────────
	private int calcDailyDriving(Long driverId, LocalDateTime now) {
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		long count = gpsDataRepository.countByDriverAndDateRange(driverId, todayStart, now);
		return (int) (count * GPS_INTERVAL_MINUTES);
	}

	private int dailyScore(int minutes) {
		if (minutes >= 600) return 45;
		if (minutes >= 480) return 30;
		if (minutes >= 360) return 15;
		return 0;
	}

	// ── 야간 운행 계산 ──────────────────────────────────────────────
	private int calcNightDriving(Long driveLogId) {
		long count = gpsDataRepository.countNightByDriveLogId(driveLogId);
		return (int) (count * GPS_INTERVAL_MINUTES);
	}

	private int nightScore(int minutes) {
		if (minutes >= 120) return 35;
		if (minutes >=  60) return 20;
		if (minutes >=  30) return 10;
		return 0;
	}

	// ── 휴식 위반 계산 ──────────────────────────────────────────────
	private int calcRestViolation(Long driveLogId, LocalDateTime startedAt,
		LocalDateTime now, int validRestCount) {
		// 2시간마다 1회 유효 휴식 필요
		int requiredRests = (int) java.time.Duration.between(startedAt, now).toMinutes() / 120;
		return Math.max(0, requiredRests - validRestCount);
	}

	private int violationScore(int violations) {
		if (violations >= 2) return 25;
		if (violations == 1) return 10;
		return 0;
	}

	// ── 휴식 보정 ──────────────────────────────────────────────────
	private int correctionScore(RestType lastRestType) {
		if (lastRestType == RestType.SUFFICIENT) return 20;
		if (lastRestType == RestType.VALID)      return 10;
		return 0;
	}

	// ── 등급 결정 ──────────────────────────────────────────────────
	private FatigueLevel toLevel(int score) {
		if (score >= 70) return FatigueLevel.DANGER;
		if (score >= 40) return FatigueLevel.CAUTION;
		return FatigueLevel.NORMAL;
	}

	// ── 근거 문자열 ────────────────────────────────────────────────
	private String buildReason(int continuous, int daily, int night,
		int violations, RestType lastRest) {
		StringBuilder sb = new StringBuilder();
		if      (continuous >= 240) sb.append("연속운행 240분 이상(+65) ");
		else if (continuous >= 180) sb.append("연속운행 180분 이상(+45) ");
		else if (continuous >= 120) sb.append("연속운행 120분 이상(+25) ");
		else if (continuous >=  90) sb.append("연속운행 90분 이상(+10) ");

		if      (daily >= 600) sb.append("일일운행 10시간 이상(+45) ");
		else if (daily >= 480) sb.append("일일운행 8시간 이상(+30) ");
		else if (daily >= 360) sb.append("일일운행 6시간 이상(+15) ");

		if      (night >= 120) sb.append("야간운행 2시간 이상(+35) ");
		else if (night >=  60) sb.append("야간운행 1시간 이상(+20) ");
		else if (night >=  30) sb.append("야간운행 30분 이상(+10) ");

		if      (violations >= 2) sb.append("휴식누락 2회 이상(+25) ");
		else if (violations == 1) sb.append("휴식누락 1회(+10) ");

		if      (lastRest == RestType.SUFFICIENT) sb.append("충분휴식 보정(-20)");
		else if (lastRest == RestType.VALID)      sb.append("유효휴식 보정(-10)");

		return sb.toString().trim();
	}
}
