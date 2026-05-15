package com.project.logmile.domain.drivelog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "시뮬레이션 종료 요청")
public record SimulationStopRequest(
	@Schema(description = "시연용 직접 입력 종료 시각", nullable = true)
	LocalDateTime endedAt
) {
}
