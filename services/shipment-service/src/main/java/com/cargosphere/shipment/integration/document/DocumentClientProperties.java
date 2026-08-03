package com.cargosphere.shipment.integration.document;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.document-client")
public class DocumentClientProperties {

    private String baseUrl = "http://localhost:8084";

    private Duration connectTimeout =
            Duration.ofSeconds(2);

    private Duration readTimeout =
            Duration.ofSeconds(3);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            this.baseUrl = "http://localhost:8084";
            return;
        }

        this.baseUrl =
                removeTrailingSlash(baseUrl.trim());
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

    private String removeTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(
                    0,
                    value.length() - 1
            );
        }

        return value;
    }
}
