package com.project.logmile.domain.drivelog.controller;

import com.project.logmile.domain.drivelog.dto.DriveHistoryDetailResponse;
import com.project.logmile.domain.drivelog.dto.DriveHistoryListResponse;
import com.project.logmile.domain.drivelog.dto.GpsPointResponse;
import com.project.logmile.domain.drivelog.service.DriveHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DriveHistory", description = "운행 이력 조회 API")
@RestController
@RequestMapping({"/api/drive-history", "/api/drive-logs"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class DriveHistoryController {

	private final DriveHistoryService driveHistoryService;

	@Operation(summary = "운행 이력 목록 조회",
		description = "본인 회사의 운행 이력을 최신순으로 조회합니다.")
	@GetMapping
	public ResponseEntity<List<DriveHistoryListResponse>> findAll() {
		return ResponseEntity.ok(driveHistoryService.findAll());
	}

	@Operation(summary = "운행 이력 상세 조회",
		description = "특정 운행의 피로도 이벤트와 휴식 이벤트를 포함한 상세 정보를 조회합니다.")
	@GetMapping("/{driveLogId}")
	public ResponseEntity<DriveHistoryDetailResponse> findById(@PathVariable Long driveLogId) {
		return ResponseEntity.ok(driveHistoryService.findById(driveLogId));
	}

	@Operation(summary = "운행 GPS 경로 조회",
		description = "특정 운행의 GPS 포인트 목록을 시간 순으로 반환합니다.")
	@GetMapping("/{driveLogId}/gps")
	public ResponseEntity<List<GpsPointResponse>> getGpsRoute(@PathVariable Long driveLogId) {
		return ResponseEntity.ok(driveHistoryService.getGpsRoute(driveLogId));
	}
}
