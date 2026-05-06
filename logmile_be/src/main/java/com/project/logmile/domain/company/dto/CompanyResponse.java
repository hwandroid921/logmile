package com.project.logmile.domain.company.dto;

import com.project.logmile.domain.company.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "업체 응답")
public record CompanyResponse(

	@Schema(description = "업체 ID", example = "1")
	Long id,

	@Schema(description = "업체명", example = "로그마일운송")
	String name,

	@Schema(description = "업체 주소", example = "서울특별시 중구 세종대로 110")
	String address,

	@Schema(description = "업체 연락처", example = "02-1000-0001")
	String phone,

	@Schema(description = "활성 여부", example = "true")
	Boolean active,

	@Schema(description = "등록 일시")
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
