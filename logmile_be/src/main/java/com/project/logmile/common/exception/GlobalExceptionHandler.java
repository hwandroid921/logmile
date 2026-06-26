package com.project.logmile.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		log.warn("BusinessException: {}", e.getMessage());
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.getStatus())
			.body(ErrorResponse.of(code, e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(FieldError::getDefaultMessage)
			.orElse(ErrorCode.INVALID_INPUT.getMessage());
		return ResponseEntity.badRequest()
			.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unhandled exception", e);
		return ResponseEntity.internalServerError()
			.body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
	}
}
