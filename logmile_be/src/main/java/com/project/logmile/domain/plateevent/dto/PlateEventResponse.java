package com.project.logmile.domain.plateevent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.logmile.common.enums.PlateEventType;
import com.project.logmile.common.enums.PlateLocationType;
import com.project.logmile.common.enums.PlateSourceType;
import com.project.logmile.domain.plateevent.entity.PlateEvent;
import java.time.LocalDateTime;

public record PlateEventResponse(
	Long id,
	Long vehicleId,
	String plateNo,
	PlateEventType eventType,
	PlateLocationType locationType,
	PlateSourceType sourceType,
	LocalDateTime observedAt,
	Double latitude,
	Double longitude,
	Double confidence,
	Double detectionConfidence,
	@JsonProperty("isManualRequired")
	Boolean manualRequired,
	String imagePath,
	String memo,
	LocalDateTime createdAt
) {
	public static PlateEventResponse from(PlateEvent event) {
		Long vehicleId = event.getVehicle() == null ? null : event.getVehicle().getId();
		return new PlateEventResponse(
			event.getId(),
			vehicleId,
			event.getPlateNo(),
			event.getEventType(),
			event.getLocationType(),
			event.getSourceType(),
			event.getObservedAt(),
			event.getLatitude(),
			event.getLongitude(),
			event.getConfidence(),
			event.getDetectionConfidence(),
			event.getManualRequired(),
			event.getImagePath(),
			event.getMemo(),
			event.getCreatedAt()
		);
	}
}
