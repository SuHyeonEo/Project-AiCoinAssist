package com.aicoinassist.api.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Operational", description = "Operational health and readiness endpoints")
@RestController
@RequestMapping("/api/health")
public class ApiHealthController {

	@Value("${spring.application.name:ai-coin-assist-api}")
	private String applicationName;

	@Operation(summary = "API health check", description = "Returns a lightweight public health response for the API server.")
	@GetMapping
	public ApiHealthResponse health() {
		return new ApiHealthResponse("UP", applicationName, Instant.now());
	}
}
