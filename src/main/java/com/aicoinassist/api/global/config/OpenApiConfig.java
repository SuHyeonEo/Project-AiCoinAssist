package com.aicoinassist.api.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI apiOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("AI Coin Assist API")
				.version("v1")
				.description("Read-oriented API server for report retrieval, asset summary, and operational endpoints.")
				.contact(new Contact().name("AI Coin Assist"))
				.license(new License().name("Proprietary")));
	}
}
