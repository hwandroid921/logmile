package com.project.logmile.domain.fatigue.controller;

import com.project.logmile.domain.fatigue.dto.FatigueStatsResponse;
import com.project.logmile.domain.fatigue.service.FatigueStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FatigueStats", description = "피로도 통계 API")
@RestController
@RequestMapping("/api/fatigue/stats")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class FatigueStatsController {

	private final FatigueStatsService fatigueStatsService;

	@Operation(summary = "일별 피로도 통계 조회",
		description = "소속 업체 기준으로 일별 운행, 야간 운행, 휴식 누락, 평균 피로 점수를 조회합니다.")
	@GetMapping
	public ResponseEntity<List<FatigueStatsResponse>> getDailyStats(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate from,
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate to,
		@RequestParam(required = false)
		Integer days) {
		return ResponseEntity.ok(fatigueStatsService.getDailyStats(from, to, days));
	}
}
