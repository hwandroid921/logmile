package com.project.logmile.domain.drivelog.entity;

import com.project.logmile.common.enums.DriveLogStatus;
import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.enums.ScenarioType;
import com.project.logmile.domain.company.entity.Company;
import com.project.logmile.domain.driver.entity.Driver;
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
@Table(name = "drive_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@Column(name = "started_at", nullable = false)
	private LocalDateTime startedAt;

	@Column(name = "ended_at")
	private LocalDateTime endedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "scenario_type", nullable = false, length = 10)
	private ScenarioType scenarioType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DriveLogStatus status = DriveLogStatus.RUNNING;

	@Column(name = "recognized_plate_no", length = 20)
	private String recognizedPlateNo;

	@Column(name = "ocr_confidence")
	private Double ocrConfidence;

	@Column(name = "is_manual_input", nullable = false)
	private Boolean manualInput = false;

	@Column(name = "total_driving_minutes")
	private Integer totalDrivingMinutes;

	@Column(name = "night_driving_minutes")
	private Integer nightDrivingMinutes;

	@Column(name = "total_rest_count")
	private Integer totalRestCount;

	@Column(name = "max_fatigue_score")
	private Integer maxFatigueScore;

	@Enumerated(EnumType.STRING)
	@Column(name = "max_fatigue_level", length = 20)
	private FatigueLevel maxFatigueLevel;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (status == null) {
			status = DriveLogStatus.RUNNING;
		}
		if (manualInput == null) {
			manualInput = false;
		}
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
