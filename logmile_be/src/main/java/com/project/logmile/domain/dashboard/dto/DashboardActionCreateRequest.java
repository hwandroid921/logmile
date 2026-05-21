package com.project.logmile.domain.dashboard.dto;

import com.project.logmile.common.enums.DashboardActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "대시보드 관제 액션 생성 요청")
public record DashboardActionCreateRequest(

	@Schema(description = "운행 ID", example = "12")
	@NotNull
	Long driveLogId,

	@Schema(description = "액션 유형", example = "REST_GUIDE")
	@NotNull
	DashboardActionType actionType,

	@Schema(description = "관리자 메모", example = "휴게소 진입 안내")
	@Size(max = 255)
	String note
) {
}
