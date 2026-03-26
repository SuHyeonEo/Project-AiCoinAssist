package com.aicoinassist.api.global.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
	List<String> allowedOrigins,
	List<String> allowedMethods,
	List<String> allowedHeaders,
	List<String> exposedHeaders,
	boolean allowCredentials,
	Long maxAge
) {

	public CorsProperties {
		allowedOrigins = defaultIfEmpty(allowedOrigins, List.of(
			"http://localhost:3000",
			"http://127.0.0.1:3000"
		));
		allowedMethods = defaultIfEmpty(allowedMethods, List.of(
			"GET",
			"POST",
			"PUT",
			"PATCH",
			"DELETE",
			"OPTIONS"
		));
		allowedHeaders = defaultIfEmpty(allowedHeaders, List.of("*"));
		exposedHeaders = defaultIfEmpty(exposedHeaders, List.of());
		maxAge = maxAge == null ? 3600L : maxAge;
	}

	private static List<String> defaultIfEmpty(List<String> value, List<String> defaultValue) {
		return value == null || value.isEmpty() ? defaultValue : List.copyOf(value);
	}
}
