package com.project.logmile.domain.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "업체 등록 요청")
public record CompanyRegisterRequest(

	@Schema(description = "업체명", example = "로그마일운송")
	@NotBlank(message = "업체명은 필수입니다.")
	String name,

	@Schema(description = "업체 주소", example = "서울특별시 중구 세종대로 110")
	String address,

	@Schema(description = "업체 연락처", example = "02-1000-0001")
	String phone
) {}
