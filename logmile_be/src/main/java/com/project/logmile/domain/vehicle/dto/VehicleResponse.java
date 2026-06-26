package com.project.logmile.domain.vehicle.dto;

import com.project.logmile.domain.vehicle.entity.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "차량 응답")
public record VehicleResponse(

	@Schema(description = "차량 ID", example = "1")
	Long id,

	@Schema(description = "소속 업체 ID", example = "1")
	Long companyId,

	@Schema(description = "번호판", example = "12가 3456")
	String plateNo,

	@Schema(description = "차종", example = "5톤 카고")
	String type,

	@Schema(description = "배정 운전자 ID", nullable = true)
	Long driverId,

	@Schema(description = "활성 여부")
	Boolean active,

	@Schema(description = "등록 일시")
	LocalDateTime createdAt
) {
	public static VehicleResponse from(Vehicle v) {
		return new VehicleResponse(
			v.getId(),
			v.getCompany() != null ? v.getCompany().getId() : null,
			v.getPlateNo(),
			v.getType(),
			v.getDriver() != null ? v.getDriver().getId() : null,
			v.getActive(),
			v.getCreatedAt()
		);
	}
}
