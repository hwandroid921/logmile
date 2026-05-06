package com.project.logmile.domain.admin.entity;

import com.project.logmile.common.enums.AdminRole;
import com.project.logmile.common.enums.AdminStatus;
import com.project.logmile.domain.company.entity.Company;
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
@Table(name = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(length = 20)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AdminRole role = AdminRole.ROLE_ADMIN;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AdminStatus status = AdminStatus.ACTIVE;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static Admin create(String email, String encodedPassword,
		String name, String phone, Company company) {
		Admin admin    = new Admin();
		admin.email    = email;
		admin.password = encodedPassword;
		admin.name     = name;
		admin.phone    = phone;
		admin.company  = company;
		admin.role     = AdminRole.ROLE_ADMIN;
		admin.status   = AdminStatus.PENDING;
		return admin;
	}

	public boolean isSuperAdmin() {
		return role == AdminRole.ROLE_SUPER_ADMIN;
	}

	public void approve()   { this.status = AdminStatus.ACTIVE;    }
	public void reject()    { this.status = AdminStatus.REJECTED;  }
	public void suspend()   { this.status = AdminStatus.SUSPENDED; }
	public void unsuspend() { this.status = AdminStatus.ACTIVE;    }

	@PrePersist
	void prePersist() {
		if (role == null) {
			role = AdminRole.ROLE_ADMIN;
		}
		if (status == null) {
			status = AdminStatus.ACTIVE;
		}
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
