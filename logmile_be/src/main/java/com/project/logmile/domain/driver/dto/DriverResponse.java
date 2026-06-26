package com.project.logmile.domain.driver.dto;

import com.project.logmile.domain.driver.entity.Driver;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "운전자 응답")
public record DriverResponse(

	@Schema(description = "운전자 ID", example = "1")
	Long id,

	@Schema(description = "소속 업체 ID", example = "1")
	Long companyId,

	@Schema(description = "운전자 이름", example = "김철수")
	String name,

	@Schema(description = "연락처", example = "010-1111-2222")
	String phone,

	@Schema(description = "면허 종류", example = "1종 대형")
	String licenseType,

	@Schema(description = "배정 차량 ID", nullable = true)
	Long vehicleId,

	@Schema(description = "등록 일시")
	LocalDateTime createdAt
) {
	public static DriverResponse from(Driver d) {
		return new DriverResponse(
			d.getId(),
			d.getCompany() != null ? d.getCompany().getId() : null,
			d.getName(),
			d.getPhone(),
			d.getLicenseType(),
			d.getVehicle() != null ? d.getVehicle().getId() : null,
			d.getCreatedAt()
		);
	}
}
