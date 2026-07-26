package com.cargosphere.audit.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.internal-audit")
public class InternalAuditProperties {

    @NotBlank(message = "Internal audit API key is required")
    @Size(
            min = 32,
            max = 256,
            message = "Internal audit API key must contain between 32 and 256 characters"
    )
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}