package com.project.logmile.domain.drivelog.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.drivelog.dto.SimulationFatigueEventRequest;
import com.project.logmile.domain.drivelog.dto.SimulationFatigueEventResponse;
import com.project.logmile.domain.drivelog.dto.SimulationRestEventRequest;
import com.project.logmile.domain.drivelog.dto.SimulationRestEventResponse;
import com.project.logmile.domain.drivelog.dto.SimulationStartRequest;
import com.project.logmile.domain.drivelog.dto.SimulationStartResponse;
import com.project.logmile.domain.drivelog.dto.SimulationStopRequest;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.driver.entity.Driver;
import com.project.logmile.domain.driver.repository.DriverRepository;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import com.project.logmile.domain.fatigue.service.FatigueScoreService;
import com.project.logmile.domain.rest.entity.RestEvent;
import com.project.logmile.domain.rest.repository.RestEventRepository;
import com.project.logmile.domain.vehicle.entity.Vehicle;
import com.project.logmile.domain.vehicle.repository.VehicleRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationService {

	private final DriveLogRepository driveLogRepository;
	private final VehicleRepository vehicleRepository;
	private final DriverRepository driverRepository;
	private final FatigueEventRepository fatigueEventRepository;
	private final RestEventRepository restEventRepository;
	private final FatigueScoreService fatigueScoreService;
	private final TenantAccessService tenantAccessService;

	@Transactional
	public SimulationStartResponse start(SimulationStartRequest request) {
		Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
		Driver driver = driverRepository.findById(request.driverId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

		tenantAccessService.validate(vehicle.getCompany().getId());
		if (!vehicle.getCompany().getId().equals(driver.getCompany().getId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "차량과 운전자의 업체가 일치해야 합니다.");
		}

		DriveLog driveLog = DriveLog.create(
			vehicle.getCompany(), vehicle, driver,
			request.scenarioType(), request.recognizedPlateNo(),
			request.ocrConfidence(), request.manualInput(), request.startedAt()
		);
		return SimulationStartResponse.from(driveLogRepository.save(driveLog));
	}

	@Transactional
	public void stop(Long driveLogId, SimulationStopRequest request) {
		DriveLog driveLog = getDriveLog(driveLogId);
		tenantAccessService.validate(driveLog.getCompany().getId());
		LocalDateTime endedAt = request != null && request.endedAt() != null
			? request.endedAt()
			: LocalDateTime.now();
		if (endedAt.isBefore(driveLog.getStartedAt())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "종료 시각은 시작 시각 이후여야 합니다.");
		}

		// 요약 집계
		List<RestEvent> validRests = restEventRepository
			.findByDriveLogIdOrderByRestStartedAtAsc(driveLogId)
			.stream()
			.filter(r -> r.getRestType() == com.project.logmile.common.enums.RestType.VALID
				|| r.getRestType() == com.project.logmile.common.enums.RestType.SUFFICIENT)
			.toList();

		List<FatigueEvent> fatigueEvents =
			fatigueEventRepository.findByDriveLogIdOrderByOccurredAtAsc(driveLogId);

		int maxScore = fatigueEvents.stream()
			.mapToInt(FatigueEvent::getFatigueScore).max().orElse(0);
		com.project.logmile.common.enums.FatigueLevel maxLevel = fatigueEvents.stream()
			.max(Comparator.comparingInt(FatigueEvent::getFatigueScore))
			.map(FatigueEvent::getFatigueLevel)
			.orElse(com.project.logmile.common.enums.FatigueLevel.NORMAL);

		long drivingMinutes = Duration.between(driveLog.getStartedAt(), endedAt).toMinutes();

		long nightMinutes = fatigueEvents.isEmpty() ? 0 :
			fatigueEvents.get(fatigueEvents.size() - 1).getNightDrivingMinutes();

		driveLog.completeAt(
			endedAt,
			(int) drivingMinutes,
			(int) nightMinutes,
			validRests.size(),
			maxScore,
			maxLevel
		);
	}

	@Transactional
	public SimulationRestEventResponse createRestEvent(SimulationRestEventRequest request) {
		DriveLog driveLog = getDriveLog(request.driveLogId());
		tenantAccessService.validate(driveLog.getCompany().getId());
		if (!request.restEndedAt().isAfter(request.restStartedAt())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "휴식 종료 시각은 시작 시각 이후여야 합니다.");
		}

		RestEvent restEvent = RestEvent.start(driveLog, request.restStartedAt());
		restEvent.end(request.restEndedAt(), 15, 30);
		return SimulationRestEventResponse.from(restEventRepository.save(restEvent));
	}

	@Transactional
	public SimulationFatigueEventResponse createFatigueEvent(SimulationFatigueEventRequest request) {
		DriveLog driveLog = getDriveLog(request.driveLogId());
		tenantAccessService.validate(driveLog.getCompany().getId());
		FatigueEvent event = fatigueScoreService.calculateManual(
			driveLog,
			request.continuousDrivingMinutes(),
			request.dailyTotalDrivingMinutes(),
			request.nightDrivingMinutes(),
			request.restCount(),
			request.restViolationCount(),
			request.reason(),
			request.occurredAt()
		);
		return SimulationFatigueEventResponse.from(event);
	}

	private DriveLog getDriveLog(Long id) {
		return driveLogRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
	}
}
