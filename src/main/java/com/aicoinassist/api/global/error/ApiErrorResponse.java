package com.aicoinassist.api.global.error;

import java.time.Instant;

public record ApiErrorResponse(
	Instant timestamp,
	int status,
	String code,
	String message,
	String path
) {
}
