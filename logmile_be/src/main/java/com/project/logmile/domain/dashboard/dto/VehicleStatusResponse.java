package com.project.logmile.domain.dashboard.dto;

import com.project.logmile.common.enums.FatigueLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "차량별 현재 피로도 상태 응답")
public record VehicleStatusResponse(

	@Schema(description = "DriveLog ID")
	Long driveLogId,

	@Schema(description = "차량 ID")
	Long vehicleId,

	@Schema(description = "차량 번호판")
	String plateNo,

	@Schema(description = "차량 유형")
	String vehicleType,

	@Schema(description = "운전자 ID")
	Long driverId,

	@Schema(description = "운전자 이름")
	String driverName,

	@Schema(description = "현재 피로도 점수 (마지막 GPS 수신 기준)")
	Integer fatigueScore,

	@Schema(description = "현재 피로도 등급")
	FatigueLevel fatigueLevel,

	@Schema(description = "운행 시작 시각")
	LocalDateTime startedAt
) {

	public static VehicleStatusResponse of(
		Long driveLogId, Long vehicleId, String plateNo, String vehicleType,
		Long driverId, String driverName,
		Integer fatigueScore, FatigueLevel fatigueLevel, LocalDateTime startedAt) {
		return new VehicleStatusResponse(
			driveLogId, vehicleId, plateNo, vehicleType,
			driverId, driverName,
			fatigueScore, fatigueLevel, startedAt);
	}
}
