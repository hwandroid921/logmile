package com.project.logmile.domain.gps.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "GPS 데이터 수신 요청")
public record GpsDataRequest(

	@Schema(description = "운행 로그 ID", example = "1")
	@NotNull(message = "driveLogId는 필수입니다.")
	Long driveLogId,

	@Schema(description = "위도", example = "37.5665")
	@NotNull(message = "위도는 필수입니다.")
	Double latitude,

	@Schema(description = "경도", example = "126.9780")
	@NotNull(message = "경도는 필수입니다.")
	Double longitude,

	@Schema(description = "속도 (km/h)", example = "60.5")
	@NotNull(message = "속도는 필수입니다.")
	@PositiveOrZero(message = "속도는 0 이상이어야 합니다.")
	Double speedKmh,

	@Schema(description = "GPS 기록 시각")
	@NotNull(message = "recordedAt은 필수입니다.")
	LocalDateTime recordedAt
) {}
