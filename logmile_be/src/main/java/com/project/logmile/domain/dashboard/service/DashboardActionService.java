package com.project.logmile.domain.dashboard.service;

import com.project.logmile.common.enums.DashboardActionType;
import com.project.logmile.common.enums.FatigueLevel;
import com.project.logmile.common.exception.BusinessException;
import com.project.logmile.common.exception.ErrorCode;
import com.project.logmile.common.security.TenantAccessService;
import com.project.logmile.domain.admin.entity.Admin;
import com.project.logmile.domain.dashboard.dto.DashboardActionCreateRequest;
import com.project.logmile.domain.dashboard.dto.DashboardActionResponse;
import com.project.logmile.domain.dashboard.entity.DashboardAction;
import com.project.logmile.domain.dashboard.repository.DashboardActionRepository;
import com.project.logmile.domain.drivelog.entity.DriveLog;
import com.project.logmile.domain.drivelog.repository.DriveLogRepository;
import com.project.logmile.domain.fatigue.entity.FatigueEvent;
import com.project.logmile.domain.fatigue.repository.FatigueEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardActionService {

	private final DashboardActionRepository dashboardActionRepository;
	private final DriveLogRepository driveLogRepository;
	private final FatigueEventRepository fatigueEventRepository;
	private final TenantAccessService tenantAccessService;

	@Transactional
	public DashboardActionResponse createAction(DashboardActionCreateRequest request) {
		Admin admin = tenantAccessService.getCurrentAdmin();

		DriveLog driveLog = driveLogRepository.findById(request.driveLogId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
		tenantAccessService.validate(driveLog.getCompany().getId());

		if (request.actionType() == DashboardActionType.PHONE_RECOMMENDATION
			&& resolveFatigueLevel(driveLog) != FatigueLevel.DANGER) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "위험 등급 차량만 전화 권고를 기록할 수 있습니다.");
		}

		// 전화 권고는 중복 방지 (같은 운행에 한 번만), 휴게 안내는 누적 허용
		if (request.actionType() == DashboardActionType.PHONE_RECOMMENDATION) {
			return dashboardActionRepository
				.findTopByDriveLogIdAndActionTypeOrderByCreatedAtDesc(
					driveLog.getId(), request.actionType())
				.map(DashboardActionResponse::from)
				.orElseGet(() -> saveNewAction(admin, driveLog, request));
		}
		return saveNewAction(admin, driveLog, request);
	}

	private DashboardActionResponse saveNewAction(Admin admin, DriveLog driveLog,
		DashboardActionCreateRequest request) {
		String note = buildNote(request, driveLog, admin);
		DashboardAction action = DashboardAction.create(
			admin.getCompany(), driveLog, admin,
			request.actionType(), note
		);
		return DashboardActionResponse.from(dashboardActionRepository.save(action));
	}

	private String buildNote(DashboardActionCreateRequest request, DriveLog driveLog, Admin admin) {
		if (request.note() != null && !request.note().isBlank()) {
			return request.note();
		}
		String driverName = driveLog.getDriver().getName();
		String companyName = admin.getCompany().getName();
		return switch (request.actionType()) {
			case REST_GUIDE -> "[%s 관제센터] %s 기사님, 안전 운행을 위해 즉시 휴식을 취해주시기 바랍니다.".formatted(companyName, driverName);
			case PHONE_RECOMMENDATION -> "[%s 관제센터] %s 기사님, 피로도가 위험 수준입니다. 즉시 차량을 안전한 곳에 정차하고 휴식하세요.".formatted(companyName, driverName);
		};
	}

	private FatigueLevel resolveFatigueLevel(DriveLog driveLog) {
		FatigueEvent latest = fatigueEventRepository
			.findTopByDriveLogIdOrderByOccurredAtDesc(driveLog.getId())
			.orElse(null);
		if (latest != null && latest.getFatigueLevel() != null) {
			return latest.getFatigueLevel();
		}
		return driveLog.getMaxFatigueLevel() != null
			? driveLog.getMaxFatigueLevel()
			: FatigueLevel.NORMAL;
	}
}
