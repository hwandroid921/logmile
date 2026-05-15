package com.project.logmile.domain.drivelog.dto;

import com.project.logmile.common.enums.RestType;
import com.project.logmile.domain.rest.entity.RestEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "시연용 휴식 이벤트 응답")
public record SimulationRestEventResponse(
	Long id,
	Long driveLogId,
	LocalDateTime restStartedAt,
	LocalDateTime restEndedAt,
	Integer restMinutes,
	RestType restType
) {
	public static SimulationRestEventResponse from(RestEvent event) {
		return new SimulationRestEventResponse(
			event.getId(),
			event.getDriveLog().getId(),
			event.getRestStartedAt(),
			event.getRestEndedAt(),
			event.getRestMinutes(),
			event.getRestType()
		);
	}
}
