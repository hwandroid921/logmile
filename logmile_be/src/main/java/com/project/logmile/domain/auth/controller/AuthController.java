package com.project.logmile.domain.auth.controller;

import com.project.logmile.common.exception.ErrorResponse;
import com.project.logmile.domain.auth.dto.LoginRequest;
import com.project.logmile.domain.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API (로그인 · 회원가입)")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	// AuthService 는 feature/be-auth-signup 브랜치에서 구현
	// 현재는 Swagger 문서 구조 확인용 스텁

	@Operation(summary = "관리자 로그인", description = "이메일/패스워드로 JWT 토큰을 발급합니다. ACTIVE 상태의 계정만 로그인 가능합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 성공",
			content = @Content(schema = @Schema(implementation = LoginResponse.class))),
		@ApiResponse(responseCode = "401", description = "이메일 또는 패스워드 불일치",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "403", description = "PENDING/REJECTED/SUSPENDED 계정",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
		// TODO: feature/be-auth-signup 에서 AuthService.login() 호출로 교체
		throw new UnsupportedOperationException("feature/be-auth-signup 브랜치에서 구현 예정");
	}
}
