package com.project.logmile.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
	int status,
	String code,
	String message,
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
