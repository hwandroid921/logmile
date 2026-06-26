package com.project.logmile.domain.driver.controller;

import com.project.logmile.domain.driver.dto.DriverRequest;
import com.project.logmile.domain.driver.dto.DriverResponse;
import com.project.logmile.domain.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Driver", description = "운전자 관리 API")
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DriverController {

	private final DriverService driverService;

	@Operation(summary = "운전자 등록")
	@PostMapping
	public ResponseEntity<DriverResponse> register(@RequestBody @Valid DriverRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(driverService.register(request));
	}

	@Operation(summary = "소속 업체 운전자 목록 조회")
	@GetMapping
	public ResponseEntity<List<DriverResponse>> findAll() {
		return ResponseEntity.ok(driverService.findAll());
	}

	@Operation(summary = "운전자 상세 조회")
	@GetMapping("/{id}")
	public ResponseEntity<DriverResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(driverService.findById(id));
	}

	@Operation(summary = "운전자 정보 수정")
	@PatchMapping("/{id}")
	public ResponseEntity<DriverResponse> update(@PathVariable Long id,
		@RequestBody @Valid DriverRequest request) {
		return ResponseEntity.ok(driverService.update(id, request));
	}

	@Operation(summary = "운전자 삭제")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		driverService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "차량 배정")
	@PatchMapping("/{id}/assign/{vehicleId}")
	public ResponseEntity<DriverResponse> assignVehicle(@PathVariable Long id,
		@PathVariable Long vehicleId) {
		return ResponseEntity.ok(driverService.assignVehicle(id, vehicleId));
	}

	@Operation(summary = "차량 배정 해제")
	@PatchMapping("/{id}/unassign")
	public ResponseEntity<DriverResponse> unassignVehicle(@PathVariable Long id) {
		return ResponseEntity.ok(driverService.unassignVehicle(id));
	}
}
