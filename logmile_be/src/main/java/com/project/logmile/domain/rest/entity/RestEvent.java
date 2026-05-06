package com.project.logmile.domain.rest.entity;

import com.project.logmile.common.enums.RestType;
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
@Table(name = "rest_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "drive_log_id", nullable = false)
	private DriveLog driveLog;

	@Column(name = "rest_started_at", nullable = false)
	private LocalDateTime restStartedAt;

	@Column(name = "rest_ended_at")
	private LocalDateTime restEndedAt;

	@Column(name = "rest_minutes")
	private Integer restMinutes;

	@Enumerated(EnumType.STRING)
	@Column(name = "rest_type", nullable = false, length = 20)
	private RestType restType = RestType.PENDING;

	@PrePersist
	void prePersist() {
		if (restType == null) {
			restType = RestType.PENDING;
		}
	}
}
