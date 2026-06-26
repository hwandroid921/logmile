package com.project.logmile.domain.fatigue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "피로도 임계값 수정 요청")
public record ThresholdUpdateRequest(

	@Schema(description = "새로운 임계값", example = "120")
	@NotNull(message = "임계값은 필수입니다.")
	@Positive(message = "임계값은 양수여야 합니다.")
	Double thresholdValue,

	@Schema(description = "설명 (선택)")
	String description
) {
}
