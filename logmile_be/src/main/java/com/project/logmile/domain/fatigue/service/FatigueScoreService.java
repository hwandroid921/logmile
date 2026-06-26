package com.project.logmile.domain.fatigue.service;

import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.enums.RestType;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.entity.FatigueThreshold;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import com.project.logmile.domain.fatigue.repository.FatigueThresholdRepository;
import com.project.logmile.domain.gps.repository.GpsDataRepository;
import com.project.logmile.domain.rest.entity.RestEvent;
import com.project.logmile.domain.rest.repository.RestEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
	private final FatigueThresholdRepository fatigueThresholdRepository;

	/**
	 * DB에서 임계값 전체를 key → value 맵으로 로드한다.
	 * DB 조회 실패 시 빈 맵을 반환하고, 각 계산 메서드에서 기본값(fallback)을 사용한다.
	 */
	private Map<String, Double> loadThresholdMap() {
		try {
			return fatigueThresholdRepository.findAll()
				.stream()
				.collect(Collectors.toMap(
					t -> t.getThresholdKey().toUpperCase(),
					FatigueThreshold::getThresholdValue
				));
		} catch (Exception e) {
			return Map.of();
		}
	}

	/** 맵에서 정수 값을 읽고, 없으면 fallback 을 반환한다. */
	private int t(Map<String, Double> map, String key, int fallback) {
		Double v = map.get(key);
		return v != null ? v.intValue() : fallback;
	}

	/**
	 * GPS 수신마다 호출. 피로도 점수를 계산하고 FatigueEvent를 저장한다.
	 */
	@Transactional
	public FatigueEvent calculate(DriveLog driveLog, LocalDateTime recordedAt) {
		Long driveLogId = driveLog.getId();
		Long driverId   = driveLog.getDriver().getId();

		Map<String, Double> T = loadThresholdMap();

		// 1. 연속 운행 시간 (분)
		int continuousMinutes = calcContinuousDriving(driveLogId, driveLog.getStartedAt(), recordedAt, T);

		// 2. 일일 총 운행 시간 (분)
		int dailyMinutes = calcDailyDriving(driverId, recordedAt);

		// 3. 야간 운행 시간 (분)
		int nightMinutes = calcNightDriving(driveLogId);

		// 4. 휴식 횟수 및 위반 횟수
		List<RestType> validTypes = List.of(RestType.VALID, RestType.SUFFICIENT);
		int restCount      = (int) restEventRepository.countByDriveLogIdAndRestTypeIn(driveLogId, validTypes);
		int violationCount = calcRestViolation(driveLogId, driveLog.getStartedAt(), recordedAt, restCount, T);

		// 5. 최근 휴식 타입 (보정용)
		Optional<RestEvent> lastRest = restEventRepository
			.findTopByDriveLogIdAndRestTypeInOrderByRestEndedAtDesc(driveLogId, validTypes);

		// 6. 점수 계산
		int score = 0;
		score += continuousScore(continuousMinutes, T);
		score += dailyScore(dailyMinutes, T);
		score += nightScore(nightMinutes, T);
		score += violationScore(violationCount, T);
		score -= correctionScore(lastRest.map(RestEvent::getRestType).orElse(null), T);
		score = Math.max(0, score);

		// 7. 등급
		FatigueLevel level = toLevel(score, T);

		// 8. 근거 문자열
		String reason = buildReason(continuousMinutes, dailyMinutes, nightMinutes,
			violationCount, lastRest.map(RestEvent::getRestType).orElse(null), T);

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

		Map<String, Double> T = loadThresholdMap();

		Optional<RestEvent> lastRest = restEventRepository
			.findTopByDriveLogIdAndRestTypeInOrderByRestEndedAtDesc(
				driveLog.getId(), List.of(RestType.VALID, RestType.SUFFICIENT));

		int score = 0;
		score += continuousScore(continuousMinutes, T);
		score += dailyScore(dailyMinutes, T);
		score += nightScore(nightMinutes, T);
		score += violationScore(violationCount, T);
		score -= correctionScore(lastRest.map(RestEvent::getRestType).orElse(null), T);
		score = Math.max(0, score);

		FatigueLevel level = toLevel(score, T);
		String reason = manualReason != null && !manualReason.isBlank()
			? manualReason
			: buildReason(continuousMinutes, dailyMinutes, nightMinutes,
				violationCount, lastRest.map(RestEvent::getRestType).orElse(null), T);

		FatigueEvent event = FatigueEvent.create(driveLog, score, level,
			continuousMinutes, dailyMinutes, nightMinutes,
			restCount, violationCount, reason, occurredAt);
		FatigueEvent saved = fatigueEventRepository.save(event);
		driveLog.updateMaxFatigue(score, level);
		return saved;
	}

	// ── 연속 운행 계산 ─────────────────────────────────────────────
	private int calcContinuousDriving(Long driveLogId, LocalDateTime startedAt,
		LocalDateTime now, Map<String, Double> T) {
		Optional<RestEvent> lastValidRest = restEventRepository
			.findTopByDriveLogIdAndRestTypeInOrderByRestEndedAtDesc(
				driveLogId, List.of(RestType.VALID, RestType.SUFFICIENT));

		LocalDateTime base = lastValidRest.map(RestEvent::getRestEndedAt).orElse(startedAt);
		return (int) java.time.Duration.between(base, now).toMinutes();
	}

	private int continuousScore(int minutes, Map<String, Double> T) {
		if (minutes >= t(T, "CONTINUOUS_DRIVING_240", 240)) return t(T, "CONTINUOUS_DRIVING_240_DELTA", 65);
		if (minutes >= t(T, "CONTINUOUS_DRIVING_180", 180)) return t(T, "CONTINUOUS_DRIVING_180_DELTA", 45);
		if (minutes >= t(T, "CONTINUOUS_DRIVING_120", 120)) return t(T, "CONTINUOUS_DRIVING_120_DELTA", 25);
		if (minutes >= t(T, "CONTINUOUS_DRIVING_90",   90)) return t(T, "CONTINUOUS_DRIVING_90_DELTA",  10);
		return 0;
	}

	// ── 일일 운행 계산 ──────────────────────────────────────────────
	private int calcDailyDriving(Long driverId, LocalDateTime now) {
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		long count = gpsDataRepository.countByDriverAndDateRange(driverId, todayStart, now);
		return (int) (count * GPS_INTERVAL_MINUTES);
	}

	private int dailyScore(int minutes, Map<String, Double> T) {
		if (minutes >= t(T, "DAILY_DRIVING_600", 600)) return t(T, "DAILY_DRIVING_600_DELTA", 45);
		if (minutes >= t(T, "DAILY_DRIVING_480", 480)) return t(T, "DAILY_DRIVING_480_DELTA", 30);
		if (minutes >= t(T, "DAILY_DRIVING_360", 360)) return t(T, "DAILY_DRIVING_360_DELTA", 15);
		return 0;
	}

	// ── 야간 운행 계산 ──────────────────────────────────────────────
	private int calcNightDriving(Long driveLogId) {
		long count = gpsDataRepository.countNightByDriveLogId(driveLogId);
		return (int) (count * GPS_INTERVAL_MINUTES);
	}

	private int nightScore(int minutes, Map<String, Double> T) {
		if (minutes >= t(T, "NIGHT_DRIVING_120", 120)) return t(T, "NIGHT_DRIVING_120_DELTA", 35);
		if (minutes >= t(T, "NIGHT_DRIVING_60",   60)) return t(T, "NIGHT_DRIVING_60_DELTA",  20);
		if (minutes >= t(T, "NIGHT_DRIVING_30",   30)) return t(T, "NIGHT_DRIVING_30_DELTA",  10);
		return 0;
	}

	// ── 휴식 위반 계산 ──────────────────────────────────────────────
	private int calcRestViolation(Long driveLogId, LocalDateTime startedAt,
		LocalDateTime now, int validRestCount, Map<String, Double> T) {
		int requiredAfter = t(T, "REST_REQUIRED_AFTER", 120);
		int requiredRests = (int) java.time.Duration.between(startedAt, now).toMinutes() / requiredAfter;
		return Math.max(0, requiredRests - validRestCount);
	}

	private int violationScore(int violations, Map<String, Double> T) {
		if (violations >= 2) return t(T, "REST_VIOLATION_TWICE_SCORE", 25);
		if (violations == 1) return t(T, "REST_VIOLATION_ONCE_SCORE",  10);
		return 0;
	}

	// ── 휴식 보정 ──────────────────────────────────────────────────
	private int correctionScore(RestType lastRestType, Map<String, Double> T) {
		if (lastRestType == RestType.SUFFICIENT) return t(T, "REST_CORRECTION_SUFFICIENT_SCORE", 20);
		if (lastRestType == RestType.VALID)      return t(T, "REST_CORRECTION_VALID_SCORE",      10);
		return 0;
	}

	// ── 등급 결정 ──────────────────────────────────────────────────
	private FatigueLevel toLevel(int score, Map<String, Double> T) {
		if (score >= t(T, "LEVEL_DANGER_MIN",  70)) return FatigueLevel.DANGER;
		if (score >= t(T, "LEVEL_CAUTION_MIN", 40)) return FatigueLevel.CAUTION;
		return FatigueLevel.NORMAL;
	}

	// ── 근거 문자열 ────────────────────────────────────────────────
	private String buildReason(int continuous, int daily, int night,
		int violations, RestType lastRest, Map<String, Double> T) {
		StringBuilder sb = new StringBuilder();

		if      (continuous >= t(T, "CONTINUOUS_DRIVING_240", 240))
			sb.append(String.format("연속운행 240분 이상(+%d) ", t(T, "CONTINUOUS_DRIVING_240_DELTA", 65)));
		else if (continuous >= t(T, "CONTINUOUS_DRIVING_180", 180))
			sb.append(String.format("연속운행 180분 이상(+%d) ", t(T, "CONTINUOUS_DRIVING_180_DELTA", 45)));
		else if (continuous >= t(T, "CONTINUOUS_DRIVING_120", 120))
			sb.append(String.format("연속운행 120분 이상(+%d) ", t(T, "CONTINUOUS_DRIVING_120_DELTA", 25)));
		else if (continuous >= t(T, "CONTINUOUS_DRIVING_90",   90))
			sb.append(String.format("연속운행 90분 이상(+%d) ",  t(T, "CONTINUOUS_DRIVING_90_DELTA",  10)));

		if      (daily >= t(T, "DAILY_DRIVING_600", 600))
			sb.append(String.format("일일운행 10시간 이상(+%d) ", t(T, "DAILY_DRIVING_600_DELTA", 45)));
		else if (daily >= t(T, "DAILY_DRIVING_480", 480))
			sb.append(String.format("일일운행 8시간 이상(+%d) ",  t(T, "DAILY_DRIVING_480_DELTA", 30)));
		else if (daily >= t(T, "DAILY_DRIVING_360", 360))
			sb.append(String.format("일일운행 6시간 이상(+%d) ",  t(T, "DAILY_DRIVING_360_DELTA", 15)));

		if      (night >= t(T, "NIGHT_DRIVING_120", 120))
			sb.append(String.format("야간운행 2시간 이상(+%d) ",  t(T, "NIGHT_DRIVING_120_DELTA", 35)));
		else if (night >= t(T, "NIGHT_DRIVING_60",   60))
			sb.append(String.format("야간운행 1시간 이상(+%d) ",  t(T, "NIGHT_DRIVING_60_DELTA",  20)));
		else if (night >= t(T, "NIGHT_DRIVING_30",   30))
			sb.append(String.format("야간운행 30분 이상(+%d) ",   t(T, "NIGHT_DRIVING_30_DELTA",  10)));

		if      (violations >= 2)
			sb.append(String.format("휴식누락 2회 이상(+%d) ", t(T, "REST_VIOLATION_TWICE_SCORE", 25)));
		else if (violations == 1)
			sb.append(String.format("휴식누락 1회(+%d) ",      t(T, "REST_VIOLATION_ONCE_SCORE",  10)));

		if      (lastRest == RestType.SUFFICIENT)
			sb.append(String.format("충분휴식 보정(-%d)", t(T, "REST_CORRECTION_SUFFICIENT_SCORE", 20)));
		else if (lastRest == RestType.VALID)
			sb.append(String.format("유효휴식 보정(-%d)", t(T, "REST_CORRECTION_VALID_SCORE",      10)));

		return sb.toString().trim();
	}
}
