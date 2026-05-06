package com.project.logmile.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "차량 등록/수정 요청")
public record VehicleRequest(

	@Schema(description = "차량 번호판", example = "12가 3456")
	@NotBlank(message = "번호판은 필수입니다.")
	String plateNo,

	@Schema(description = "차종", example = "5톤 카고")
	@NotBlank(message = "차종은 필수입니다.")
	String type
) {}
