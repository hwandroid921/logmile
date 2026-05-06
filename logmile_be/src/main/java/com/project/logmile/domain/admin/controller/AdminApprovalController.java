package com.project.logmile.domain.admin.controller;

import com.project.logmile.common.exception.ErrorResponse;
import com.project.logmile.domain.admin.dto.AdminSummaryResponse;
import com.project.logmile.domain.admin.service.AdminApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Approval", description = "관리자 승인 API (SUPER_ADMIN 전용)")
@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {

	private final AdminApprovalService adminApprovalService;

	@Operation(summary = "승인 대기 목록 조회", description = "PENDING 상태의 일반 관리자 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/pending")
	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
	public ResponseEntity<List<AdminSummaryResponse>> getPendingAdmins() {
		return ResponseEntity.ok(adminApprovalService.getPendingAdmins());
	}

	@Operation(summary = "가입 승인", description = "PENDING 상태의 관리자를 ACTIVE로 변경합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "승인 성공"),
		@ApiResponse(responseCode = "400", description = "PENDING 상태가 아닌 계정",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "관리자를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PatchMapping("/{adminId}/approve")
	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
	public ResponseEntity<Void> approve(@PathVariable Long adminId) {
		adminApprovalService.approve(adminId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "가입 거절", description = "관리자 계정을 REJECTED로 변경합니다.")
	@ApiResponse(responseCode = "204", description = "거절 성공")
	@PatchMapping("/{adminId}/reject")
	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
	public ResponseEntity<Void> reject(@PathVariable Long adminId) {
		adminApprovalService.reject(adminId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "계정 정지", description = "관리자 계정을 SUSPENDED로 변경합니다. 최상위 관리자는 정지 불가.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "정지 성공"),
		@ApiResponse(responseCode = "403", description = "최상위 관리자 정지 시도",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PatchMapping("/{adminId}/suspend")
	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
	public ResponseEntity<Void> suspend(@PathVariable Long adminId) {
		adminApprovalService.suspend(adminId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "정지 해제", description = "SUSPENDED 상태의 관리자를 ACTIVE로 변경합니다.")
	@ApiResponse(responseCode = "204", description = "해제 성공")
	@PatchMapping("/{adminId}/unsuspend")
	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
	public ResponseEntity<Void> unsuspend(@PathVariable Long adminId) {
		adminApprovalService.unsuspend(adminId);
		return ResponseEntity.noContent().build();
	}
}
