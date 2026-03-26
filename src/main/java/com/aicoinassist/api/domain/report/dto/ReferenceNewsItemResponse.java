package com.aicoinassist.api.domain.report.dto;

import java.time.Instant;

public record ReferenceNewsItemResponse(
	String title,
	String source,
	Instant publishedAt,
	String url,
	String whyItMatters,
	String relatedDomain
) {
}
