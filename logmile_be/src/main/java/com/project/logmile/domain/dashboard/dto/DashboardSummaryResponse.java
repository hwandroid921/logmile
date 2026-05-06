package com.project.logmile.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 요약 응답")
public record DashboardSummaryResponse(

	@Schema(description = "현재 운행 중인 차량 수")
	int runningVehicles,

	@Schema(description = "주의 단계(CAUTION) 차량 수")
	int cautionVehicles,

	@Schema(description = "위험 단계(DANGER) 차량 수")
	int dangerVehicles,

	@Schema(description = "오늘 완료된 운행 수")
	int todayCompleted,

	@Schema(description = "오늘 평균 피로도 점수")
	double avgFatigueScore
) {

	public static DashboardSummaryResponse of(int running, int caution, int danger,
		int todayCompleted, double avgScore) {
		return new DashboardSummaryResponse(running, caution, danger, todayCompleted, avgScore);
	}
}
