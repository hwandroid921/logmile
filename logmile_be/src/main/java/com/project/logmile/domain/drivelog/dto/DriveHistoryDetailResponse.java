package com.project.logmile.domain.drivelog.dto;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.enums.ScenarioType;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.rest.entity.RestEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "운행 이력 상세 응답")
public record DriveHistoryDetailResponse(

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

	@Schema(description = "운전자 연락처")
	String driverPhone,

	@Schema(description = "시나리오 유형")
	ScenarioType scenarioType,

	@Schema(description = "운행 상태")
	DriveLogStatus status,

	@Schema(description = "인식된 번호판")
	String recognizedPlateNo,

	@Schema(description = "OCR 신뢰도")
	Double ocrConfidence,

	@Schema(description = "수동 입력 여부")
	Boolean manualInput,

	@Schema(description = "운행 시작 시각")
	LocalDateTime startedAt,

	@Schema(description = "운행 종료 시각")
	LocalDateTime endedAt,

	@Schema(description = "총 운행 시간 (분)")
	Integer totalDrivingMinutes,

	@Schema(description = "야간 운행 시간 (분)")
	Integer nightDrivingMinutes,

	@Schema(description = "총 휴식 횟수")
	Integer totalRestCount,

	@Schema(description = "최대 피로도 점수")
	Integer maxFatigueScore,

	@Schema(description = "최대 피로도 등급")
	FatigueLevel maxFatigueLevel,

	@Schema(description = "피로도 이벤트 목록 (시계열)")
	List<FatigueEventSummary> fatigueEvents,

	@Schema(description = "휴식 이벤트 목록")
	List<RestEventSummary> restEvents
) {

	public record FatigueEventSummary(
		Long id,
		Integer fatigueScore,
		FatigueLevel fatigueLevel,
		Integer continuousDrivingMinutes,
		String reason,
		LocalDateTime occurredAt
	) {
		public static FatigueEventSummary from(FatigueEvent e) {
			return new FatigueEventSummary(
				e.getId(),
				e.getFatigueScore(),
				e.getFatigueLevel(),
				e.getContinuousDrivingMinutes(),
				e.getReason(),
				e.getOccurredAt()
			);
		}
	}

	public record RestEventSummary(
		Long id,
		String restType,
		Integer restMinutes,
		LocalDateTime restStartedAt,
		LocalDateTime restEndedAt
	) {
		public static RestEventSummary from(RestEvent r) {
			return new RestEventSummary(
				r.getId(),
				r.getRestType().name(),
				r.getRestMinutes(),
				r.getRestStartedAt(),
				r.getRestEndedAt()
			);
		}
	}

	public static DriveHistoryDetailResponse from(DriveLog log,
		List<FatigueEvent> fatigueEvents, List<RestEvent> restEvents) {
		return new DriveHistoryDetailResponse(
			log.getId(),
			log.getVehicle().getId(),
			log.getVehicle().getPlateNo(),
			log.getVehicle().getType(),
			log.getDriver().getId(),
			log.getDriver().getName(),
			log.getDriver().getPhone(),
			log.getScenarioType(),
			log.getStatus(),
			log.getRecognizedPlateNo(),
			log.getOcrConfidence(),
			log.getManualInput(),
			log.getStartedAt(),
			log.getEndedAt(),
			log.getTotalDrivingMinutes(),
			log.getNightDrivingMinutes(),
			log.getTotalRestCount(),
			log.getMaxFatigueScore(),
			log.getMaxFatigueLevel(),
			fatigueEvents.stream().map(FatigueEventSummary::from).toList(),
			restEvents.stream().map(RestEventSummary::from).toList()
		);
	}
}
