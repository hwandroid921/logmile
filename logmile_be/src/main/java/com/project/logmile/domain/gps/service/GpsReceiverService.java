package com.project.logmile.domain.gps.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.fatigue.service.FatigueScoreService;
import com.project.logmile.domain.gps.dto.GpsDataRequest;
import com.project.logmile.domain.gps.entity.GpsData;
import com.project.logmile.domain.gps.repository.GpsDataRepository;
import com.project.logmile.domain.rest.service.RestEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GpsReceiverService {

	private final GpsDataRepository gpsDataRepository;
	private final DriveLogRepository driveLogRepository;
	private final RestEventService restEventService;
	private final FatigueScoreService fatigueScoreService;

	@Transactional
	public GpsData receive(GpsDataRequest request) {
		DriveLog driveLog = driveLogRepository.findById(request.driveLogId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

		// 1. GPS 데이터 저장
		GpsData gpsData = GpsData.create(
			driveLog,
			request.latitude(),
			request.longitude(),
			request.speedKmh(),
			request.recordedAt()
		);
		gpsDataRepository.save(gpsData);

		// 2. 휴식 판단 (정차/주행 전환 감지)
		restEventService.process(driveLog, request.speedKmh(), request.recordedAt());

		// 3. 피로도 점수 계산 및 저장
		fatigueScoreService.calculate(driveLog, request.recordedAt());

		return gpsData;
	}
}
