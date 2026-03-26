package com.aicoinassist.batch;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@EnableScheduling
@ConfigurationPropertiesScan(basePackages = {"com.aicoinassist.batch", "com.aicoinassist.api"})
@EntityScan(basePackages = {"com.aicoinassist.batch"})
@EnableJpaRepositories(basePackages = {"com.aicoinassist.batch"})
@SpringBootApplication(scanBasePackages = {"com.aicoinassist.batch", "com.aicoinassist.api"})
public class BatchApplication {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}
}
