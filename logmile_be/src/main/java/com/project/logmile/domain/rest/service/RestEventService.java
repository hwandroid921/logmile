package com.project.logmile.domain.rest.service;

import com.project.logmile.common.enums.RestType;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.rest.entity.RestEvent;
import com.project.logmile.domain.rest.repository.RestEventRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestEventService {

	// seed 임계값 기준 (추후 FatigueThresholdService 연동 가능)
	private static final int VALID_MINUTES      = 15;
	private static final int SUFFICIENT_MINUTES = 30;
	private static final double STOP_SPEED_KMH  = 3.0;

	private final RestEventRepository restEventRepository;

	/**
	 * GPS 수신마다 호출.
	 * speedKmh <= 3  → 정차: PENDING RestEvent 없으면 생성
	 * speedKmh > 3   → 주행: PENDING RestEvent 있으면 종료 처리
	 */
	@Transactional
	public void process(DriveLog driveLog, double speedKmh, LocalDateTime recordedAt) {
		Optional<RestEvent> pendingOpt = restEventRepository
			.findTopByDriveLogIdAndRestTypeOrderByRestStartedAtDesc(
				driveLog.getId(), RestType.PENDING);

		if (speedKmh <= STOP_SPEED_KMH) {
			// 정차 중 — PENDING이 없으면 새로 시작
			if (pendingOpt.isEmpty()) {
				restEventRepository.save(RestEvent.start(driveLog, recordedAt));
			}
		} else {
			// 주행 중 — 진행 중인 PENDING이 있으면 종료
			pendingOpt.ifPresent(rest -> {
				rest.end(recordedAt, VALID_MINUTES, SUFFICIENT_MINUTES);
			});
		}
	}
}
