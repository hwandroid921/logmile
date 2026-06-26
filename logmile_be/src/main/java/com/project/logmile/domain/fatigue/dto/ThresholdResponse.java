package com.project.logmile.domain.fatigue.dto;

import com.project.logmile.domain.fatigue.entity.FatigueThreshold;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "피로도 임계값 응답")
public record ThresholdResponse(

	@Schema(description = "임계값 ID")
	Long id,

	@Schema(description = "임계값 키 (예: continuous_danger_minutes)")
	String thresholdKey,

	@Schema(description = "임계값")
	Double thresholdValue,

	@Schema(description = "설명")
	String description,

	@Schema(description = "마지막 수정 시각")
	LocalDateTime updatedAt
) {

	public static ThresholdResponse from(FatigueThreshold threshold) {
		return new ThresholdResponse(
			threshold.getId(),
			threshold.getThresholdKey(),
			threshold.getThresholdValue(),
			threshold.getDescription(),
			threshold.getUpdatedAt()
		);
	}
}
