package com.project.logmile.domain.drivelog.dto;

import com.project.logmile.common.enums.ScenarioType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "시뮬레이션 시작 요청")
public record SimulationStartRequest(

	@Schema(description = "차량 ID", example = "1")
	@NotNull(message = "vehicleId는 필수입니다.")
	Long vehicleId,

	@Schema(description = "운전자 ID", example = "1")
	@NotNull(message = "driverId는 필수입니다.")
	Long driverId,

	@Schema(description = "시나리오 유형 (A: 정상, B: 주의, C: 위험)", example = "A")
	@NotNull(message = "scenarioType은 필수입니다.")
	ScenarioType scenarioType,

	@Schema(description = "인식된 번호판", nullable = true)
	String recognizedPlateNo,

	@Schema(description = "OCR 신뢰도 (0.0~1.0)", nullable = true)
	Double ocrConfidence,

	@Schema(description = "수동 입력 여부", example = "false")
	Boolean manualInput
) {}
