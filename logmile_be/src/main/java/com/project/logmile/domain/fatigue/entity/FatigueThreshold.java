package com.project.logmile.domain.fatigue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "fatigue_threshold")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FatigueThreshold {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "threshold_key", nullable = false, unique = true, length = 100)
	private String thresholdKey;

	@Column(name = "threshold_value", nullable = false)
	private Double thresholdValue;

	@Column(length = 255)
	private String description;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public void updateValue(Double newValue, String newDescription) {
		if (newValue != null) {
			this.thresholdValue = newValue;
		}
		if (newDescription != null) {
			this.description = newDescription;
		}
	}

	@PrePersist
	@PreUpdate
	void updateTimestamp() {
		updatedAt = LocalDateTime.now();
	}
}
