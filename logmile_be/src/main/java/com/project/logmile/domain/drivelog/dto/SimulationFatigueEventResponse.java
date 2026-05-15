package com.project.logmile.domain.drivelog.dto;

import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "시연용 피로도 이벤트 응답")
public record SimulationFatigueEventResponse(
	Long id,
	Long driveLogId,
	Integer fatigueScore,
	FatigueLevel fatigueLevel,
	Integer continuousDrivingMinutes,
	Integer dailyTotalDrivingMinutes,
	Integer nightDrivingMinutes,
	Integer restCount,
	Integer restViolationCount,
	String reason,
	LocalDateTime occurredAt
) {
	public static SimulationFatigueEventResponse from(FatigueEvent event) {
		return new SimulationFatigueEventResponse(
			event.getId(),
			event.getDriveLog().getId(),
			event.getFatigueScore(),
			event.getFatigueLevel(),
			event.getContinuousDrivingMinutes(),
			event.getDailyTotalDrivingMinutes(),
			event.getNightDrivingMinutes(),
			event.getRestCount(),
			event.getRestViolationCount(),
			event.getReason(),
			event.getOccurredAt()
		);
	}
}
