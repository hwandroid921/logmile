package com.project.logmile.domain.dashboard.dto;

import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "대시보드 피로도 이벤트 응답")
public record DashboardEventResponse(

	@Schema(description = "DriveLog ID")
	Long driveLogId,

	@Schema(description = "차량 번호판")
	String plateNo,

	@Schema(description = "운전자 이름")
	String driverName,

	@Schema(description = "피로도 점수")
	Integer fatigueScore,

	@Schema(description = "피로도 등급")
	FatigueLevel fatigueLevel,

	@Schema(description = "연속 운행 시간 (분)")
	Integer continuousDrivingMinutes,

	@Schema(description = "일일 누적 운행 시간 (분)")
	Integer dailyTotalDrivingMinutes,

	@Schema(description = "야간 운행 시간 (분)")
	Integer nightDrivingMinutes,

	@Schema(description = "피로도 이벤트 발생 사유")
	String reason,

	@Schema(description = "이벤트 발생 시각")
	LocalDateTime occurredAt
) {

	public static DashboardEventResponse from(FatigueEvent e) {
		return new DashboardEventResponse(
			e.getDriveLog().getId(),
			e.getDriveLog().getVehicle().getPlateNo(),
			e.getDriveLog().getDriver().getName(),
			e.getFatigueScore(),
			e.getFatigueLevel(),
			e.getContinuousDrivingMinutes(),
			e.getDailyTotalDrivingMinutes(),
			e.getNightDrivingMinutes(),
			e.getReason(),
			e.getOccurredAt()
		);
	}
}
