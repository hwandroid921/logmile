package com.project.logmile.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(

	@Schema(description = "관리자 이메일", example = "admin@logmile.com")
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	@Schema(description = "패스워드", example = "admin1234")
	@NotBlank(message = "패스워드는 필수입니다.")
	String password
) {}
