package com.project.logmile.domain.drivelog.dto;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.common.enums.ScenarioType;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "시뮬레이션 시작 응답")
public record SimulationStartResponse(

	@Schema(description = "운행 로그 ID", example = "1")
	Long driveLogId,

	@Schema(description = "차량 ID", example = "1")
	Long vehicleId,

	@Schema(description = "운전자 ID", example = "1")
	Long driverId,

	@Schema(description = "시나리오 유형")
	ScenarioType scenarioType,

	@Schema(description = "운행 상태")
	DriveLogStatus status,

	@Schema(description = "시작 시각")
	LocalDateTime startedAt
) {
	public static SimulationStartResponse from(DriveLog d) {
		return new SimulationStartResponse(
			d.getId(), d.getVehicle().getId(), d.getDriver().getId(),
			d.getScenarioType(), d.getStatus(), d.getStartedAt()
		);
	}
}
