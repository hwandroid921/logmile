package com.project.logmile.domain.driver.entity;

import com.project.logmile.domain.company.entity.Company;
import com.project.logmile.domain.vehicle.entity.Vehicle;
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
@Table(name = "driver")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Driver {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(name = "license_type", nullable = false, length = 30)
	private String licenseType;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", unique = true)
	private Vehicle vehicle;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static Driver create(Company company, String name, String phone, String licenseType) {
		Driver d       = new Driver();
		d.company      = company;
		d.name         = name;
		d.phone        = phone;
		d.licenseType  = licenseType;
		return d;
	}

	public void update(String name, String phone, String licenseType) {
		if (name        != null) this.name        = name;
		if (phone       != null) this.phone       = phone;
		if (licenseType != null) this.licenseType = licenseType;
	}

	public void assignVehicle(Vehicle vehicle)  { this.vehicle = vehicle; }
	public void unassignVehicle()               { this.vehicle = null;    }

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
