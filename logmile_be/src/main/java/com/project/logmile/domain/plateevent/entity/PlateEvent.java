package com.project.logmile.domain.plateevent.entity;

import com.project.logmile.common.enums.PlateEventType;
import com.project.logmile.common.enums.PlateLocationType;
import com.project.logmile.common.enums.PlateSourceType;
import com.project.logmile.domain.vehicle.entity.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "plate_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlateEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id")
	private Vehicle vehicle;

	@Column(name = "plate_no", nullable = false, length = 20)
	private String plateNo;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 20)
	private PlateEventType eventType;

	@Enumerated(EnumType.STRING)
	@Column(name = "location_type", nullable = false, length = 30)
	private PlateLocationType locationType;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 20)
	private PlateSourceType sourceType;

	@Column(name = "observed_at", nullable = false)
	private LocalDateTime observedAt;

	@Column
	private Double latitude;

	@Column
	private Double longitude;

	@Column
	private Double confidence;

	@Column(name = "detection_confidence")
	private Double detectionConfidence;

	@Column(name = "is_manual_required", nullable = false)
	private Boolean manualRequired = false;

	@Column(name = "image_path", length = 500)
	private String imagePath;

	@Column(columnDefinition = "TEXT")
	private String memo;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static PlateEvent create(
		Vehicle vehicle,
		String plateNo,
		PlateEventType eventType,
		PlateLocationType locationType,
		PlateSourceType sourceType,
		LocalDateTime observedAt,
		Double latitude,
		Double longitude,
		Double confidence,
		Double detectionConfidence,
		Boolean manualRequired,
		String imagePath,
		String memo
	) {
		PlateEvent plateEvent = new PlateEvent();
		plateEvent.vehicle = vehicle;
		plateEvent.plateNo = plateNo;
		plateEvent.eventType = eventType;
		plateEvent.locationType = locationType;
		plateEvent.sourceType = sourceType;
		plateEvent.observedAt = observedAt;
		plateEvent.latitude = latitude;
		plateEvent.longitude = longitude;
		plateEvent.confidence = confidence;
		plateEvent.detectionConfidence = detectionConfidence;
		plateEvent.manualRequired = manualRequired;
		plateEvent.imagePath = imagePath;
		plateEvent.memo = memo;
		return plateEvent;
	}

	@PrePersist
	void prePersist() {
		if (manualRequired == null) {
			manualRequired = false;
		}
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
