package com.project.logmile.domain.drivelog.controller;

import com.project.logmile.domain.drivelog.dto.SimulationFatigueEventRequest;
import com.project.logmile.domain.drivelog.dto.SimulationFatigueEventResponse;
import com.project.logmile.domain.drivelog.dto.SimulationRestEventRequest;
import com.project.logmile.domain.drivelog.dto.SimulationRestEventResponse;
import com.project.logmile.domain.drivelog.dto.SimulationStartRequest;
import com.project.logmile.domain.drivelog.dto.SimulationStartResponse;
import com.project.logmile.domain.drivelog.dto.SimulationStopRequest;
import com.project.logmile.domain.drivelog.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Simulation", description = "시뮬레이션 제어 API")
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class SimulationController {

	private final SimulationService simulationService;

	@Operation(summary = "시뮬레이션 시작", description = "DriveLog(RUNNING) 를 생성하고 driveLogId를 반환합니다.")
	@PostMapping("/start")
	public ResponseEntity<SimulationStartResponse> start(
		@RequestBody @Valid SimulationStartRequest request) {
		return ResponseEntity.ok(simulationService.start(request));
	}

	@Operation(summary = "시뮬레이션 종료", description = "DriveLog를 COMPLETED로 변경하고 운행 요약을 저장합니다.")
	@PatchMapping("/{driveLogId}/stop")
	public ResponseEntity<Void> stop(@PathVariable Long driveLogId,
		@RequestBody(required = false) SimulationStopRequest request) {
		simulationService.stop(driveLogId, request);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "시연용 휴식 이벤트 등록",
		description = "휴게소/CCTV 자동 감지 흐름을 시연에서 직접 입력한 시간으로 대체해 RestEvent를 저장합니다.")
	@PostMapping("/rest-events")
	public ResponseEntity<SimulationRestEventResponse> createRestEvent(
		@RequestBody @Valid SimulationRestEventRequest request) {
		return ResponseEntity.ok(simulationService.createRestEvent(request));
	}

	@Operation(summary = "시연용 피로도 이벤트 등록",
		description = "GPS 기반 운행 시간 계산 흐름을 시연에서 직접 입력한 운행 지표로 대체해 FatigueEvent를 저장합니다.")
	@PostMapping("/fatigue-events")
	public ResponseEntity<SimulationFatigueEventResponse> createFatigueEvent(
		@RequestBody @Valid SimulationFatigueEventRequest request) {
		return ResponseEntity.ok(simulationService.createFatigueEvent(request));
	}
}
