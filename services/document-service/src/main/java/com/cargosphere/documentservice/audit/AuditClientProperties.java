package com.cargosphere.documentservice.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.audit-client")
public class AuditClientProperties {

    private static final String DEFAULT_BASE_URL =
            "http://localhost:8086";

    private boolean enabled = true;

    private String baseUrl = DEFAULT_BASE_URL;

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
            this.baseUrl = DEFAULT_BASE_URL;
            return;
        }

        String normalizedUrl = baseUrl.trim();

        while (
                normalizedUrl.endsWith("/")
                        && normalizedUrl.length() > 1
        ) {
            normalizedUrl = normalizedUrl.substring(
                    0,
                    normalizedUrl.length() - 1
            );
        }

        this.baseUrl = normalizedUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null
                ? null
                : apiKey.trim();
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(
            Duration connectTimeout
    ) {
        this.connectTimeout = connectTimeout == null
                ? Duration.ofSeconds(2)
                : connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(
            Duration readTimeout
    ) {
        this.readTimeout = readTimeout == null
                ? Duration.ofSeconds(3)
                : readTimeout;
    }
}