package com.project.logmile.domain.plateevent.controller;

import com.project.logmile.common.exception.ErrorResponse;
import com.project.logmile.domain.plateevent.dto.PlateEventCreateRequest;
import com.project.logmile.domain.plateevent.dto.PlateEventResponse;
import com.project.logmile.domain.plateevent.service.PlateEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Simulation Plate Event", description = "시뮬레이터 번호판 입출차 이벤트 API")
@RestController
@RequestMapping("/api/simulation/plate-events")
@RequiredArgsConstructor
public class PlateEventController {

	private final PlateEventService plateEventService;

	@Operation(summary = "시뮬레이터 번호판 이벤트 등록",
		description = "직접 입력, 더미, OCR 기반 번호판 입출차 이벤트를 저장합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "번호판 이벤트 등록 성공",
			content = @Content(schema = @Schema(implementation = PlateEventResponse.class))),
		@ApiResponse(responseCode = "400", description = "요청값 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping
	public ResponseEntity<PlateEventResponse> create(@RequestBody @Valid PlateEventCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(plateEventService.create(request));
	}
}
