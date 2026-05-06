package com.project.logmile.domain.admin.dto;

import com.project.logmile.common.enums.AdminStatus;
import com.project.logmile.domain.admin.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자 요약 응답")
public record AdminSummaryResponse(

	@Schema(description = "관리자 ID", example = "2")
	Long adminId,

	@Schema(description = "관리자 이메일", example = "admin@logmile.com")
	String email,

	@Schema(description = "관리자 이름", example = "홍길동")
	String name,

	@Schema(description = "계정 상태", example = "PENDING")
	AdminStatus status,

	@Schema(description = "소속 업체 ID", example = "1")
	Long companyId,

	@Schema(description = "소속 업체명", example = "로그마일운송")
	String companyName,

	@Schema(description = "가입 일시")
	LocalDateTime createdAt
) {
	public static AdminSummaryResponse from(Admin admin) {
		return new AdminSummaryResponse(
			admin.getId(),
			admin.getEmail(),
			admin.getName(),
			admin.getStatus(),
			admin.getCompany() != null ? admin.getCompany().getId() : null,
			admin.getCompany() != null ? admin.getCompany().getName() : null,
			admin.getCreatedAt()
		);
	}
}
