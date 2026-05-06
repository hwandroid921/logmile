package com.project.logmile.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 인증
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED,          "인증이 필요합니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED,         "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,         "만료된 토큰입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN,            "접근 권한이 없습니다."),

	// 계정 상태
	ACCOUNT_PENDING(HttpStatus.FORBIDDEN,          "승인 대기 중인 계정입니다."),
	ACCOUNT_REJECTED(HttpStatus.FORBIDDEN,         "승인이 거절된 계정입니다."),
	ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN,        "정지된 계정입니다."),
	ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN,         "비활성화된 계정입니다."),

	// 리소스
	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND,        "업체를 찾을 수 없습니다."),
	ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND,          "관리자를 찾을 수 없습니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT,           "이미 사용 중인 이메일입니다."),
	DUPLICATE_COMPANY_NAME(HttpStatus.CONFLICT,    "이미 등록된 업체명입니다."),

	// 공통
	INVALID_INPUT(HttpStatus.BAD_REQUEST,          "잘못된 입력입니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;
}
