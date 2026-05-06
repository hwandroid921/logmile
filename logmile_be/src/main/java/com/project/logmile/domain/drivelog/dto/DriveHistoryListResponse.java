package com.project.logmile.domain.drivelog.dto;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.enums.ScenarioType;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "운행 이력 목록 응답")
public record DriveHistoryListResponse(

	@Schema(description = "DriveLog ID")
	Long driveLogId,

	@Schema(description = "차량 번호판")
	String plateNo,

	@Schema(description = "운전자 이름")
	String driverName,

	@Schema(description = "시나리오 유형")
	ScenarioType scenarioType,

	@Schema(description = "운행 상태")
	DriveLogStatus status,

	@Schema(description = "운행 시작 시각")
	LocalDateTime startedAt,

	@Schema(description = "운행 종료 시각")
	LocalDateTime endedAt,

	@Schema(description = "총 운행 시간 (분)")
	Integer totalDrivingMinutes,

	@Schema(description = "최대 피로도 점수")
	Integer maxFatigueScore,

	@Schema(description = "최대 피로도 등급")
	FatigueLevel maxFatigueLevel
) {

	public static DriveHistoryListResponse from(DriveLog log) {
		return new DriveHistoryListResponse(
			log.getId(),
			log.getVehicle().getPlateNo(),
			log.getDriver().getName(),
			log.getScenarioType(),
			log.getStatus(),
			log.getStartedAt(),
			log.getEndedAt(),
			log.getTotalDrivingMinutes(),
			log.getMaxFatigueScore(),
			log.getMaxFatigueLevel()
		);
	}
}
