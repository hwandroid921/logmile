package com.project.logmile.domain.gps.controller;

import com.project.logmile.domain.gps.dto.GpsDataRequest;
import com.project.logmile.domain.gps.service.GpsReceiverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GPS", description = "GPS 데이터 수신 API")
@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
public class GpsController {

	private final GpsReceiverService gpsReceiverService;

	@Operation(summary = "GPS 데이터 수신",
		description = "시뮬레이터로부터 GPS 데이터를 수신하여 저장합니다. 저장 후 피로도 계산을 트리거합니다.")
	@PostMapping
	public ResponseEntity<Void> receive(@RequestBody @Valid GpsDataRequest request) {
		gpsReceiverService.receive(request);
		return ResponseEntity.ok().build();
	}
}
