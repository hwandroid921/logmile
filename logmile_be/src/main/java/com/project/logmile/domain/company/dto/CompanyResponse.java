package com.project.logmile.domain.company.dto;

import com.project.logmile.domain.company.entity.Company;
import java.time.LocalDateTime;

public record CompanyResponse(
	Long id,
	String name,
	String address,
	String phone,
	Boolean active,
	LocalDateTime createdAt
) {
	public static CompanyResponse from(Company company) {
		return new CompanyResponse(
			company.getId(),
			company.getName(),
			company.getAddress(),
			company.getPhone(),
			company.getActive(),
			company.getCreatedAt()
		);
	}
}
