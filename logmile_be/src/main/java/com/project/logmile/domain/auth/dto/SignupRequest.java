package com.project.logmile.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 요청")
public record SignupRequest(

	@Schema(description = "업체명", example = "로그마일운송")
	@NotBlank(message = "업체명은 필수입니다.")
	String companyName,

	@Schema(description = "업체 주소", example = "서울특별시 중구 세종대로 110")
	String address,

	@Schema(description = "업체 연락처", example = "02-1000-0001")
	String phone,

	@Schema(description = "관리자 이름", example = "홍길동")
	@NotBlank(message = "이름은 필수입니다.")
	String name,

	@Schema(description = "관리자 연락처", example = "010-1234-5678")
	String adminPhone,

	@Schema(description = "관리자 이메일", example = "admin@logmile.com")
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	@Schema(description = "패스워드", example = "password1234")
	@NotBlank(message = "패스워드는 필수입니다.")
	String password
) {}
