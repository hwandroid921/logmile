package com.project.logmile.domain.dashboard.controller;

import com.project.logmile.domain.dashboard.dto.DashboardSummaryResponse;
import com.project.logmile.domain.dashboard.dto.VehicleStatusResponse;
import com.project.logmile.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 조회 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class DashboardController {

	private final DashboardService dashboardService;

	@Operation(summary = "대시보드 요약 조회",
		description = "현재 운행 차량 수, 피로도 단계별 현황, 오늘 완료 운행 수, 평균 피로도 점수를 반환합니다.")
	@GetMapping("/summary")
	public ResponseEntity<DashboardSummaryResponse> getSummary() {
		return ResponseEntity.ok(dashboardService.getSummary());
	}

	@Operation(summary = "운행 중 차량 목록 조회",
		description = "현재 RUNNING 상태인 차량 목록과 각 차량의 최근 피로도 정보를 반환합니다.")
	@GetMapping("/vehicles")
	public ResponseEntity<List<VehicleStatusResponse>> getRunningVehicles() {
		return ResponseEntity.ok(dashboardService.getRunningVehicles());
	}
}
