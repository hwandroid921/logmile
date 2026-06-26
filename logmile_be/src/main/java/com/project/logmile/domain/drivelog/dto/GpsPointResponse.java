package com.project.logmile.domain.drivelog.dto;

import com.project.logmile.domain.gps.entity.GpsData;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "GPS 경로 포인트")
public record GpsPointResponse(

	@Schema(description = "위도")
	Double latitude,

	@Schema(description = "경도")
	Double longitude,

	@Schema(description = "속도 (km/h)")
	Double speedKmh,

	@Schema(description = "기록 시각")
	LocalDateTime recordedAt
) {
	public static GpsPointResponse from(GpsData g) {
		return new GpsPointResponse(g.getLatitude(), g.getLongitude(), g.getSpeedKmh(), g.getRecordedAt());
	}
}
