package com.project.logmile.domain.fatigue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "일별 피로도 통계 응답")
public record FatigueStatsResponse(

	@Schema(description = "집계 날짜", example = "2026-05-12")
	LocalDate date,

	@Schema(description = "운행 기록 수", example = "8")
	long driveLogCount,

	@Schema(description = "총 운행 시간(분)", example = "420")
	long totalDrivingMinutes,

	@Schema(description = "야간 운행 시간(분)", example = "90")
	long nightDrivingMinutes,

	@Schema(description = "휴식 누락 횟수", example = "2")
	long restViolationCount,

	@Schema(description = "위험 이벤트 수", example = "1")
	long dangerEventCount,

	@Schema(description = "평균 피로 점수", example = "46.1")
	double averageFatigueScore
) {
}
