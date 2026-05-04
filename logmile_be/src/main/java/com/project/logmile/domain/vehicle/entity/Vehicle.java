package com.project.logmile.domain.vehicle.entity;

import com.project.logmile.domain.company.entity.Company;
import com.project.logmile.domain.driver.entity.Driver;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "vehicle")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(name = "plate_no", nullable = false, unique = true, length = 20)
	private String plateNo;

	@Column(nullable = false, length = 50)
	private String type;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id", unique = true)
	private Driver driver;

	@Column(name = "is_active", nullable = false)
	private Boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (active == null) {
			active = true;
		}
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
