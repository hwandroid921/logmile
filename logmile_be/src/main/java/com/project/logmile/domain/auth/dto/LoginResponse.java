package com.project.logmile.domain.auth.dto;

import com.project.logmile.common.enums.AdminRole;
import com.project.logmile.common.enums.AdminStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(

	@Schema(description = "JWT 액세스 토큰")
	String accessToken,

	@Schema(description = "토큰 타입", example = "Bearer")
	String tokenType,

	@Schema(description = "관리자 ID", example = "1")
	Long adminId,

	@Schema(description = "관리자 이름", example = "홍길동")
	String name,

	@Schema(description = "권한", example = "ROLE_ADMIN")
	AdminRole role,

	@Schema(description = "계정 상태", example = "ACTIVE")
	AdminStatus status,

	@Schema(description = "소속 업체 ID (SUPER_ADMIN은 null)", example = "1", nullable = true)
	Long companyId
) {
	public static LoginResponse of(String token, Long adminId, String name,
		AdminRole role, AdminStatus status, Long companyId) {
		return new LoginResponse(token, "Bearer", adminId, name, role, status, companyId);
	}
}
