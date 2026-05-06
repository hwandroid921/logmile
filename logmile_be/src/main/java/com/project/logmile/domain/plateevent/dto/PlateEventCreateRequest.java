package com.project.logmile.domain.plateevent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.logmile.common.enums.PlateEventType;
import com.project.logmile.common.enums.PlateLocationType;
import com.project.logmile.common.enums.PlateSourceType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record PlateEventCreateRequest(
	@NotBlank
	@Size(max = 20)
	String plateNo,

	@NotNull
	PlateEventType eventType,

	@NotNull
	PlateLocationType locationType,

	@NotNull
	PlateSourceType sourceType,

	@NotNull
	LocalDateTime observedAt,

	Double latitude,

	Double longitude,

	@DecimalMin("0.0")
	@DecimalMax("1.0")
	Double confidence,

	@DecimalMin("0.0")
	@DecimalMax("1.0")
	Double detectionConfidence,

	@JsonProperty("isManualRequired")
	Boolean manualRequired,

	@Size(max = 500)
	String imagePath,

	String memo
) {
}
