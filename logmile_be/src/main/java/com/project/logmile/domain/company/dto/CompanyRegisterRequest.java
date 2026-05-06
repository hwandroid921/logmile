package com.project.logmile.domain.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest(

	@NotBlank(message = "업체명은 필수입니다.")
	@Size(max = 100)
	String name,

	@Size(max = 255)
	String address,

	@Size(max = 20)
	String phone
) {}
