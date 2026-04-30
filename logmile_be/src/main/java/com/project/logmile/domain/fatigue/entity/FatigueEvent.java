package com.project.logmile.domain.fatigue.entity;

import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.domain.drivelog.entity.DriveLog;
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
@Table(name = "fatigue_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FatigueEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "drive_log_id", nullable = false)
	private DriveLog driveLog;

	@Column(name = "fatigue_score", nullable = false)
	private Integer fatigueScore = 0;

	@Enumerated(EnumType.STRING)
	@Column(name = "fatigue_level", nullable = false, length = 20)
	private FatigueLevel fatigueLevel = FatigueLevel.NORMAL;

	@Column(name = "continuous_driving_minutes", nullable = false)
	private Integer continuousDrivingMinutes = 0;

	@Column(name = "daily_total_driving_minutes", nullable = false)
	private Integer dailyTotalDrivingMinutes = 0;

	@Column(name = "night_driving_minutes", nullable = false)
	private Integer nightDrivingMinutes = 0;

	@Column(name = "rest_count", nullable = false)
	private Integer restCount = 0;

	@Column(name = "rest_violation_count", nullable = false)
	private Integer restViolationCount = 0;

	@Column(columnDefinition = "TEXT")
	private String reason;

	@Column(name = "occurred_at", nullable = false)
	private LocalDateTime occurredAt;

	@PrePersist
	void prePersist() {
		if (fatigueScore == null) {
			fatigueScore = 0;
		}
		if (fatigueLevel == null) {
			fatigueLevel = FatigueLevel.NORMAL;
		}
		if (continuousDrivingMinutes == null) {
			continuousDrivingMinutes = 0;
		}
		if (dailyTotalDrivingMinutes == null) {
			dailyTotalDrivingMinutes = 0;
		}
		if (nightDrivingMinutes == null) {
			nightDrivingMinutes = 0;
		}
		if (restCount == null) {
			restCount = 0;
		}
		if (restViolationCount == null) {
			restViolationCount = 0;
		}
		if (occurredAt == null) {
			occurredAt = LocalDateTime.now();
		}
	}
}
