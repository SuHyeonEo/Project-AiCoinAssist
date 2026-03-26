package com.aicoinassist.api.global.error;

import com.aicoinassist.api.domain.report.exception.ReportNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ReportNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleReportNotFound(
		ReportNotFoundException exception,
		HttpServletRequest request
	) {
		return response(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", exception.getMessage(), request);
	}

	@ExceptionHandler({
		ConstraintViolationException.class,
		MethodArgumentTypeMismatchException.class,
		MethodArgumentNotValidException.class,
		HandlerMethodValidationException.class,
		IllegalArgumentException.class
	})
	public ResponseEntity<ApiErrorResponse> handleBadRequest(
		Exception exception,
		HttpServletRequest request
	) {
		return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleInternalServerError(
		Exception exception,
		HttpServletRequest request
	) {
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", exception.getMessage(), request);
	}

	private ResponseEntity<ApiErrorResponse> response(
		HttpStatus status,
		String code,
		String message,
		HttpServletRequest request
	) {
		return ResponseEntity.status(status).body(
			new ApiErrorResponse(
				Instant.now(),
				status.value(),
				code,
				message,
				request.getRequestURI()
			)
		);
	}
}
