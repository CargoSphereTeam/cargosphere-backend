package com.cargosphere.auth.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.audit-client")
public class AuditClientProperties {

    private boolean enabled = true;

    private String baseUrl =
            "http://localhost:8086";

    private String apiKey;

    private Duration connectTimeout =
            Duration.ofSeconds(2);

    private Duration readTimeout =
            Duration.ofSeconds(3);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            this.baseUrl = "http://localhost:8086";
            return;
        }

        this.baseUrl =
                removeTrailingSlash(baseUrl.trim());
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(
            Duration connectTimeout
    ) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(
            Duration readTimeout
    ) {
        this.readTimeout = readTimeout;
    }

    private String removeTrailingSlash(
            String value
    ) {
        while (value.endsWith("/")) {
            value = value.substring(
                    0,
                    value.length() - 1
            );
        }

        return value;
    }
}