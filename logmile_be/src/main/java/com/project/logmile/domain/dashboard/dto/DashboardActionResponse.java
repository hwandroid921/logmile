package com.project.logmile.domain.dashboard.dto;

import com.project.logmile.common.enums.DashboardActionType;
import com.project.logmile.domain.dashboard.entity.DashboardAction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "대시보드 관제 액션 응답")
public record DashboardActionResponse(

	@Schema(description = "액션 ID")
	Long actionId,

	@Schema(description = "운행 ID")
	Long driveLogId,

	@Schema(description = "액션 유형")
	DashboardActionType actionType,

	@Schema(description = "처리 관리자명")
	String adminName,

	@Schema(description = "운전자 이름")
	String driverName,

	@Schema(description = "운전자 전화번호")
	String driverPhone,

	@Schema(description = "SMS 시뮬레이션 메시지 내용")
	String smsMessage,

	@Schema(description = "처리 시각")
	LocalDateTime createdAt
) {

	public static DashboardActionResponse from(DashboardAction action) {
		return new DashboardActionResponse(
			action.getId(),
			action.getDriveLog().getId(),
			action.getActionType(),
			action.getAdmin().getName(),
			action.getDriveLog().getDriver().getName(),
			action.getDriveLog().getDriver().getPhone(),
			action.getNote(),
			action.getCreatedAt()
		);
	}
}
