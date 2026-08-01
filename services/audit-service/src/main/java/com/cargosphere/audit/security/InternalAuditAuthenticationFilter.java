package com.cargosphere.audit.security;

import com.cargosphere.audit.config.InternalAuditProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InternalAuditAuthenticationFilter
        extends OncePerRequestFilter {

    public static final String HEADER_NAME =
            "X-Internal-API-Key";

    private static final String INTERNAL_ENDPOINT =
            "/api/audits/internal";

    private final InternalAuditProperties properties;
    private final ObjectMapper objectMapper;

    public InternalAuditAuthenticationFilter(
            InternalAuditProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

@Override
protected boolean shouldNotFilter(
        HttpServletRequest request
) {
    String requestPath =
            request.getRequestURI()
                    .substring(
                            request.getContextPath().length()
                    );

    boolean isInternalAuditRequest =
            HttpMethod.POST.matches(request.getMethod())
                    && INTERNAL_ENDPOINT.equals(
                            requestPath
                    );

    return !isInternalAuditRequest;
}

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String suppliedKey =
                request.getHeader(HEADER_NAME);

        if (!isValidApiKey(suppliedKey)) {
            writeUnauthorizedResponse(response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "internal-audit-client",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_SERVICE"
                                )
                        )
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isValidApiKey(String suppliedKey) {
        if (suppliedKey == null || suppliedKey.isBlank()) {
            return false;
        }

        byte[] configuredBytes =
                properties.getApiKey()
                        .getBytes(StandardCharsets.UTF_8);

        byte[] suppliedBytes =
                suppliedKey.trim()
                        .getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(
                configuredBytes,
                suppliedBytes
        );
    }

    private void writeUnauthorizedResponse(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(
                HttpStatus.UNAUTHORIZED.value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.UNAUTHORIZED.value()
        );

        body.put(
                "error",
                HttpStatus.UNAUTHORIZED
                        .getReasonPhrase()
        );

        body.put(
                "message",
                "Invalid or missing internal audit API key"
        );

        body.put(
                "validationErrors",
                null
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }
}