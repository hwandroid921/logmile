package com.project.logmile.domain.drivelog.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.drivelog.dto.SimulationStartRequest;
import com.project.logmile.domain.drivelog.dto.SimulationStartResponse;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.driver.entity.Driver;
import com.project.logmile.domain.driver.repository.DriverRepository;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import com.project.logmile.domain.rest.entity.RestEvent;
import com.project.logmile.domain.rest.repository.RestEventRepository;
import com.project.logmile.domain.vehicle.entity.Vehicle;
import com.project.logmile.domain.vehicle.repository.VehicleRepository;
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
	private final TenantAccessService tenantAccessService;

	@Transactional
	public SimulationStartResponse start(SimulationStartRequest request) {
		Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
		Driver driver = driverRepository.findById(request.driverId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

		tenantAccessService.validate(vehicle.getCompany().getId());

		DriveLog driveLog = DriveLog.create(
			vehicle.getCompany(), vehicle, driver,
			request.scenarioType(), request.recognizedPlateNo(),
			request.ocrConfidence(), request.manualInput()
		);
		return SimulationStartResponse.from(driveLogRepository.save(driveLog));
	}

	@Transactional
	public void stop(Long driveLogId) {
		DriveLog driveLog = getDriveLog(driveLogId);
		tenantAccessService.validate(driveLog.getCompany().getId());

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

		long drivingMinutes = java.time.Duration.between(
			driveLog.getStartedAt(), java.time.LocalDateTime.now()).toMinutes();

		long nightMinutes = fatigueEvents.isEmpty() ? 0 :
			fatigueEvents.get(fatigueEvents.size() - 1).getNightDrivingMinutes();

		driveLog.complete(
			(int) drivingMinutes,
			(int) nightMinutes,
			validRests.size(),
			maxScore,
			maxLevel
		);
	}

	private DriveLog getDriveLog(Long id) {
		return driveLogRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
	}
}
