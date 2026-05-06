package com.project.logmile.domain.auth.controller;

import com.project.logmile.common.exception.ErrorResponse;
import com.project.logmile.domain.auth.dto.LoginRequest;
import com.project.logmile.domain.auth.dto.LoginResponse;
import com.project.logmile.domain.auth.dto.SignupRequest;
import com.project.logmile.domain.auth.dto.SignupResponse;
import com.project.logmile.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API (로그인 · 회원가입)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "일반 관리자 회원가입",
		description = "업체 정보와 관리자 정보를 입력해 가입 요청을 생성합니다. 계정 상태는 PENDING으로 설정됩니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "회원가입 성공 (승인 대기)",
			content = @Content(schema = @Schema(implementation = SignupResponse.class))),
		@ApiResponse(responseCode = "409", description = "이메일 또는 업체명 중복",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@RequestBody @Valid SignupRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
	}

	@Operation(summary = "관리자 로그인",
		description = "이메일/패스워드로 JWT 토큰을 발급합니다. ACTIVE 상태의 계정만 로그인 가능합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 성공",
			content = @Content(schema = @Schema(implementation = LoginResponse.class))),
		@ApiResponse(responseCode = "401", description = "이메일 또는 패스워드 불일치",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "403", description = "PENDING / REJECTED / SUSPENDED 계정",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}
}
