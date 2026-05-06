package com.project.logmile.domain.vehicle.controller;

import com.project.logmile.domain.vehicle.dto.VehicleRequest;
import com.project.logmile.domain.vehicle.dto.VehicleResponse;
import com.project.logmile.domain.vehicle.service.VehicleService;
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

@Tag(name = "Vehicle", description = "차량 관리 API")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class VehicleController {

	private final VehicleService vehicleService;

	@Operation(summary = "차량 등록")
	@PostMapping
	public ResponseEntity<VehicleResponse> register(@RequestBody @Valid VehicleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.register(request));
	}

	@Operation(summary = "소속 업체 차량 목록 조회")
	@GetMapping
	public ResponseEntity<List<VehicleResponse>> findAll() {
		return ResponseEntity.ok(vehicleService.findAll());
	}

	@Operation(summary = "차량 상세 조회")
	@GetMapping("/{id}")
	public ResponseEntity<VehicleResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(vehicleService.findById(id));
	}

	@Operation(summary = "차량 수정")
	@PatchMapping("/{id}")
	public ResponseEntity<VehicleResponse> update(@PathVariable Long id,
		@RequestBody @Valid VehicleRequest request) {
		return ResponseEntity.ok(vehicleService.update(id, request));
	}

	@Operation(summary = "차량 비활성화 (소프트 삭제)")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivate(@PathVariable Long id) {
		vehicleService.deactivate(id);
		return ResponseEntity.noContent().build();
	}
}
