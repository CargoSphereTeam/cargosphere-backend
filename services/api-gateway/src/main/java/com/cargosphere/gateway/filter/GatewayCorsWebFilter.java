package com.cargosphere.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GatewayCorsWebFilter
        implements WebFilter, Ordered {

    private static final List<HttpMethod>
            ALLOWED_METHODS = List.of(
                    HttpMethod.GET,
                    HttpMethod.POST,
                    HttpMethod.PUT,
                    HttpMethod.PATCH,
                    HttpMethod.DELETE,
                    HttpMethod.OPTIONS
            );

    private static final List<String>
            ALLOWED_HEADERS = List.of(
                    HttpHeaders.AUTHORIZATION,
                    HttpHeaders.CONTENT_TYPE,
                    HttpHeaders.ACCEPT,
                    "X-Request-ID"
            );

    private static final List<String>
            EXPOSED_HEADERS = List.of(
                    HttpHeaders.LOCATION,
                    "X-Request-ID"
            );

    private final String allowedOrigin;

    public GatewayCorsWebFilter(
            @Value(
                    "${app.cors.allowed-origin:"
                            + "http://localhost:5173}"
            )
            String allowedOrigin
    ) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {
        HttpHeaders requestHeaders =
                exchange.getRequest().getHeaders();

        HttpHeaders responseHeaders =
                exchange.getResponse().getHeaders();

        String origin =
                requestHeaders.getOrigin();

        /*
         * A request without Origin is not a browser
         * cross-origin request. Continue normally.
         */
        if (origin == null) {
            return chain.filter(exchange);
        }

        responseHeaders.setVary(
                List.of(
                        HttpHeaders.ORIGIN,
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_METHOD,
                        HttpHeaders
                                .ACCESS_CONTROL_REQUEST_HEADERS
                )
        );

        if (!allowedOrigin.equals(origin)) {
            exchange.getResponse().setStatusCode(
                    HttpStatus.FORBIDDEN
            );

            return exchange.getResponse().setComplete();
        }

        responseHeaders.setAccessControlAllowOrigin(
                origin
        );

        responseHeaders.setAccessControlAllowCredentials(
                true
        );

        responseHeaders.setAccessControlExposeHeaders(
                EXPOSED_HEADERS
        );

        boolean preflightRequest =
                exchange.getRequest().getMethod()
                        == HttpMethod.OPTIONS
                        && requestHeaders
                        .getAccessControlRequestMethod()
                        != null;

        if (!preflightRequest) {
            return chain.filter(exchange);
        }

        HttpMethod requestedMethod =
                requestHeaders
                        .getAccessControlRequestMethod();

        if (!ALLOWED_METHODS.contains(requestedMethod)) {
            exchange.getResponse().setStatusCode(
                    HttpStatus.FORBIDDEN
            );

            return exchange.getResponse().setComplete();
        }

        List<String> requestedHeaders =
                requestHeaders
                        .getAccessControlRequestHeaders();

        boolean headersAllowed =
                requestedHeaders
                        .stream()
                        .allMatch(
                                this::isAllowedHeader
                        );

        if (!headersAllowed) {
            exchange.getResponse().setStatusCode(
                    HttpStatus.FORBIDDEN
            );

            return exchange.getResponse().setComplete();
        }

        responseHeaders.setAccessControlAllowMethods(
                ALLOWED_METHODS
        );

        responseHeaders.setAccessControlAllowHeaders(
                ALLOWED_HEADERS
        );

        responseHeaders.set(
                HttpHeaders.ACCESS_CONTROL_MAX_AGE,
                "3600"
        );

        exchange.getResponse().setStatusCode(
                HttpStatus.OK
        );

        return exchange.getResponse().setComplete();
    }

    private boolean isAllowedHeader(
            String requestedHeader
    ) {
        return ALLOWED_HEADERS
                .stream()
                .anyMatch(
                        allowedHeader ->
                                allowedHeader
                                        .equalsIgnoreCase(
                                                requestedHeader
                                        )
                );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}