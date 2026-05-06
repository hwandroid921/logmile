package com.project.logmile.domain.drivelog.service;

import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.drivelog.dto.DriveHistoryDetailResponse;
import com.project.logmile.domain.drivelog.dto.DriveHistoryListResponse;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import com.project.logmile.domain.rest.entity.RestEvent;
import com.project.logmile.domain.rest.repository.RestEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriveHistoryService {

	private final DriveLogRepository driveLogRepository;
	private final FatigueEventRepository fatigueEventRepository;
	private final RestEventRepository restEventRepository;
	private final TenantAccessService tenantAccessService;

	/**
	 * 회사별 운행 이력 목록 (최신순)
	 */
	@Transactional(readOnly = true)
	public List<DriveHistoryListResponse> findAll() {
		Long companyId = tenantAccessService.getCurrentCompanyId();
		List<DriveLog> logs = driveLogRepository.findByCompanyIdOrderByStartedAtDesc(companyId);
		return logs.stream().map(DriveHistoryListResponse::from).toList();
	}

	/**
	 * 운행 이력 상세 (피로도 이벤트 + 휴식 이벤트 포함)
	 */
	@Transactional(readOnly = true)
	public DriveHistoryDetailResponse findById(Long driveLogId) {
		DriveLog log = driveLogRepository.findById(driveLogId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

		// 회사 접근 권한 검증
		tenantAccessService.validate(log.getCompany().getId());

		List<FatigueEvent> fatigueEvents =
			fatigueEventRepository.findByDriveLogIdOrderByOccurredAtAsc(driveLogId);

		List<RestEvent> restEvents =
			restEventRepository.findByDriveLogIdOrderByRestStartedAtAsc(driveLogId);

		return DriveHistoryDetailResponse.from(log, fatigueEvents, restEvents);
	}
}
