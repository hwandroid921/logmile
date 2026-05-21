package com.project.logmile.domain.dashboard.entity;

import com.project.logmile.common.enums.DashboardActionType;
import com.project.logmile.domain.admin.entity.Admin;
import com.project.logmile.domain.company.entity.Company;
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
@Table(name = "dashboard_action")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardAction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "drive_log_id", nullable = false)
	private DriveLog driveLog;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "admin_id", nullable = false)
	private Admin admin;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 40)
	private DashboardActionType actionType;

	@Column(length = 255)
	private String note;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static DashboardAction create(Company company, DriveLog driveLog, Admin admin,
		DashboardActionType actionType, String note) {
		DashboardAction action = new DashboardAction();
		action.company = company;
		action.driveLog = driveLog;
		action.admin = admin;
		action.actionType = actionType;
		action.note = note;
		return action;
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
