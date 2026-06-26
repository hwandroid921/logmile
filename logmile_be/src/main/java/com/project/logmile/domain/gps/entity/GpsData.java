package com.project.logmile.domain.gps.entity;

import com.project.logmile.domain.drivelog.entity.DriveLog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "gps_data")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GpsData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "drive_log_id", nullable = false)
	private DriveLog driveLog;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	@Column(name = "speed_kmh", nullable = false)
	private Double speedKmh = 0.0;

	@Column(name = "recorded_at", nullable = false)
	private LocalDateTime recordedAt;

	public static GpsData create(DriveLog driveLog, Double latitude, Double longitude,
		Double speedKmh, java.time.LocalDateTime recordedAt) {
		GpsData g    = new GpsData();
		g.driveLog   = driveLog;
		g.latitude   = latitude;
		g.longitude  = longitude;
		g.speedKmh   = speedKmh != null ? speedKmh : 0.0;
		g.recordedAt = recordedAt;
		return g;
	}

	@PrePersist
	void prePersist() {
		if (speedKmh == null) {
			speedKmh = 0.0;
		}
	}
}
