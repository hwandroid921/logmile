package com.project.logmile.domain.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "운전자 등록/수정 요청")
public record DriverRequest(

	@Schema(description = "운전자 이름", example = "김철수")
	@NotBlank(message = "이름은 필수입니다.")
	String name,

	@Schema(description = "연락처", example = "010-1111-2222")
	@NotBlank(message = "연락처는 필수입니다.")
	String phone,

	@Schema(description = "면허 종류", example = "1종 대형")
	@NotBlank(message = "면허 종류는 필수입니다.")
	String licenseType
) {}
