package com.project.logmile.domain.drivelog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "시연용 휴식 이벤트 등록 요청")
public record SimulationRestEventRequest(
	@Schema(description = "운행 로그 ID", example = "1")
	@NotNull(message = "driveLogId는 필수입니다.")
	Long driveLogId,

	@Schema(description = "휴식 시작 시각")
	@NotNull(message = "restStartedAt은 필수입니다.")
	LocalDateTime restStartedAt,

	@Schema(description = "휴식 종료 시각")
	@NotNull(message = "restEndedAt은 필수입니다.")
	LocalDateTime restEndedAt
) {
}
