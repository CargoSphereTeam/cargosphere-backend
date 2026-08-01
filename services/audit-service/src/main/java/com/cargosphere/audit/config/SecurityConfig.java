package com.cargosphere.audit.config;

import com.cargosphere.audit.security.InternalAuditAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(
        InternalAuditProperties.class
)
public class SecurityConfig {

    private static final String AUTHORITIES_CLAIM =
            "authorities";

    @Bean
    public InternalAuditAuthenticationFilter
    internalAuditAuthenticationFilter(
            InternalAuditProperties properties,
            ObjectMapper objectMapper
    ) {
        return new InternalAuditAuthenticationFilter(
                properties,
                objectMapper
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            InternalAuditAuthenticationFilter
                    internalAuditAuthenticationFilter
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(
                        AbstractHttpConfigurer::disable
                )

                .httpBasic(
                        AbstractHttpConfigurer::disable
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/audits/health",
                                        "/actuator/health",
                                        "/actuator/info"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/api/audits/**"
                                )
                                .authenticated()

                                .anyRequest()
                                .denyAll()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )

                .addFilterBefore(
                        internalAuditAuthenticationFilter,
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter
                authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName(
                AUTHORITIES_CLAIM
        );

        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter
                authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter
                .setJwtGrantedAuthoritiesConverter(
                        authoritiesConverter
                );

        return authenticationConverter;
    }
}