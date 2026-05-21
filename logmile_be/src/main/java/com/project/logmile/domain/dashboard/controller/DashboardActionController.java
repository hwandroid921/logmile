package com.project.logmile.domain.dashboard.controller;

import com.project.logmile.domain.dashboard.dto.DashboardActionCreateRequest;
import com.project.logmile.domain.dashboard.dto.DashboardActionResponse;
import com.project.logmile.domain.dashboard.service.DashboardActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard Action", description = "대시보드 관제 액션 API")
@RestController
@RequestMapping("/api/dashboard/actions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class DashboardActionController {

	private final DashboardActionService dashboardActionService;

	@Operation(summary = "대시보드 관제 액션 기록",
		description = "휴게 안내 또는 전화 권고 액션을 기록합니다. 같은 운행의 같은 액션은 중복 기록되지 않습니다.")
	@PostMapping
	public ResponseEntity<DashboardActionResponse> createAction(
		@RequestBody @Valid DashboardActionCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(dashboardActionService.createAction(request));
	}
}
