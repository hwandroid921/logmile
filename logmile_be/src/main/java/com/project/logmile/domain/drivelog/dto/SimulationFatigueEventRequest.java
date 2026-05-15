package com.project.logmile.domain.drivelog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "시연용 피로도 이벤트 등록 요청")
public record SimulationFatigueEventRequest(
	@Schema(description = "운행 로그 ID", example = "1")
	@NotNull(message = "driveLogId는 필수입니다.")
	Long driveLogId,

	@Schema(description = "연속 운행 시간(분)", example = "180")
	@NotNull(message = "continuousDrivingMinutes는 필수입니다.")
	@Min(value = 0, message = "continuousDrivingMinutes는 0 이상이어야 합니다.")
	Integer continuousDrivingMinutes,

	@Schema(description = "일일 총 운행 시간(분)", example = "420")
	@NotNull(message = "dailyTotalDrivingMinutes는 필수입니다.")
	@Min(value = 0, message = "dailyTotalDrivingMinutes는 0 이상이어야 합니다.")
	Integer dailyTotalDrivingMinutes,

	@Schema(description = "야간 운행 시간(분)", example = "60")
	@NotNull(message = "nightDrivingMinutes는 필수입니다.")
	@Min(value = 0, message = "nightDrivingMinutes는 0 이상이어야 합니다.")
	Integer nightDrivingMinutes,

	@Schema(description = "유효 휴식 횟수", example = "1")
	@NotNull(message = "restCount는 필수입니다.")
	@Min(value = 0, message = "restCount는 0 이상이어야 합니다.")
	Integer restCount,

	@Schema(description = "휴식 위반 횟수", example = "1")
	@NotNull(message = "restViolationCount는 필수입니다.")
	@Min(value = 0, message = "restViolationCount는 0 이상이어야 합니다.")
	Integer restViolationCount,

	@Schema(description = "피로도 이벤트 발생 시각", nullable = true)
	LocalDateTime occurredAt,

	@Schema(description = "시연용 입력 사유", nullable = true)
	String reason
) {
}
