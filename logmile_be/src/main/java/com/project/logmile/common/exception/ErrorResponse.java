package com.project.logmile.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "공통 오류 응답")
public record ErrorResponse(

	@Schema(description = "HTTP 상태 코드", example = "401")
	int status,

	@Schema(description = "오류 코드", example = "INVALID_TOKEN")
	String code,

	@Schema(description = "오류 메시지", example = "유효하지 않은 토큰입니다.")
	String message,

	@Schema(description = "오류 발생 시각")
	LocalDateTime timestamp
) {
	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(
			errorCode.getStatus().value(),
			errorCode.name(),
			errorCode.getMessage(),
			LocalDateTime.now()
		);
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(
			errorCode.getStatus().value(),
			errorCode.name(),
			message,
			LocalDateTime.now()
		);
	}
}
