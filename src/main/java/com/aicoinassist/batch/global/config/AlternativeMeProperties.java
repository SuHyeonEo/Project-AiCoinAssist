package com.aicoinassist.batch.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "external.alternative-me")
public class AlternativeMeProperties {

    private String baseUrl;
}
