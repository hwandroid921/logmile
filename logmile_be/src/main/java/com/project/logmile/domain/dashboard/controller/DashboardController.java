package com.project.logmile.domain.dashboard.controller;

import com.project.logmile.domain.dashboard.dto.DashboardEventResponse;
import com.project.logmile.domain.dashboard.dto.DashboardSummaryResponse;
import com.project.logmile.domain.dashboard.dto.VehicleStatusResponse;
import com.project.logmile.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
		description = "선택 날짜 기준 대시보드 요약 정보를 조회합니다. 날짜가 없으면 오늘 기준으로 조회합니다.")
	@GetMapping("/summary")
	public ResponseEntity<DashboardSummaryResponse> getSummary(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate date) {
		return ResponseEntity.ok(dashboardService.getSummary(date));
	}

	@Operation(summary = "운행 중 차량 목록 조회",
		description = "선택 날짜 기준 대시보드 차량 목록을 조회합니다. 날짜가 없으면 오늘의 RUNNING 차량을 반환합니다.")
	@GetMapping("/vehicles")
	public ResponseEntity<List<VehicleStatusResponse>> getRunningVehicles(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate date) {
		return ResponseEntity.ok(dashboardService.getRunningVehicles(date));
	}

	@Operation(summary = "대시보드 이벤트 스트림 조회",
		description = "선택 날짜 기준 최근 피로도 이벤트 30건을 최신순으로 반환합니다.")
	@GetMapping("/events")
	public ResponseEntity<List<DashboardEventResponse>> getRecentEvents(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate date) {
		return ResponseEntity.ok(dashboardService.getRecentEvents(date));
	}
}
