package com.project.logmile.domain.fatigue.controller;

import com.project.logmile.domain.fatigue.dto.ThresholdResponse;
import com.project.logmile.domain.fatigue.dto.ThresholdUpdateRequest;
import com.project.logmile.domain.fatigue.service.FatigueThresholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FatigueThreshold", description = "피로도 임계값 관리 API")
@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class FatigueThresholdController {

	private final FatigueThresholdService fatigueThresholdService;

	@Operation(summary = "전체 임계값 목록 조회")
	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
	public ResponseEntity<List<ThresholdResponse>> findAll() {
		return ResponseEntity.ok(fatigueThresholdService.findAll());
	}

	@Operation(summary = "임계값 수정", description = "SUPER_ADMIN만 임계값을 수정할 수 있습니다.")
	@PatchMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
	public ResponseEntity<ThresholdResponse> update(
		@PathVariable Long id,
		@Valid @RequestBody ThresholdUpdateRequest request) {
		return ResponseEntity.ok(fatigueThresholdService.update(id, request));
	}
}
