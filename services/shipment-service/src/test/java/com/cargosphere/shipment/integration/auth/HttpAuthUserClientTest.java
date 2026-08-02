package com.cargosphere.shipment.integration.auth;

import com.cargosphere.shipment.exception.DownstreamServiceException;

import com.cargosphere.shipment.security.CurrentJwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpAuthUserClientTest {

    @Test
    void shouldReturnUserDetailsById() {
        RestClient.Builder builder =
                RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(builder)
                        .build();

        RestClient restClient = builder
                .baseUrl("http://localhost:8081")
                .build();

        CurrentJwtTokenProvider tokenProvider =
                mock(CurrentJwtTokenProvider.class);

        when(tokenProvider.getTokenValue())
                .thenReturn("admin-token");

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8081/api/auth/users/100"
                        )
                )
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer admin-token"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "id": 100,
                                  "fullName": "Cargo Client",
                                  "email": "client@cargosphere.com",
                                  "phoneNumber": "9876543210",
                                  "role": "ROLE_CLIENT",
                                  "status": "ACTIVE",
                                  "createdAt": "2026-08-01T09:00:00",
                                  "updatedAt": "2026-08-02T10:00:00"
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        AuthUserClient client =
                new HttpAuthUserClient(
                        restClient,
                        tokenProvider
                );

        AuthUserResponse response =
                client.getUserById(100L);

        assertEquals(100L, response.id());
        assertEquals(
                "Cargo Client",
                response.fullName()
        );
        assertEquals(
                "client@cargosphere.com",
                response.email()
        );
        assertEquals(
                "9876543210",
                response.phoneNumber()
        );
        assertEquals(
                "ROLE_CLIENT",
                response.role()
        );
        assertEquals(
                "ACTIVE",
                response.status()
        );

        server.verify();
    }

    @Test
    void shouldWrapAuthServiceHttpFailure() {
        RestClient.Builder builder =
                RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(builder)
                        .build();

        RestClient restClient = builder
                .baseUrl("http://localhost:8081")
                .build();

        CurrentJwtTokenProvider tokenProvider =
                mock(CurrentJwtTokenProvider.class);

        when(tokenProvider.getTokenValue())
                .thenReturn("admin-token");

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:8081/api/auth/users/100"
                        )
                )
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer admin-token"
                        )
                )
                .andRespond(
                        withStatus(
                                HttpStatus.SERVICE_UNAVAILABLE
                        )
                );

        AuthUserClient client =
                new HttpAuthUserClient(
                        restClient,
                        tokenProvider
                );

        DownstreamServiceException exception =
                assertThrows(
                        DownstreamServiceException.class,
                        () -> client.getUserById(100L)
                );

        assertEquals(
                "auth-service",
                exception.getServiceName()
        );

        server.verify();
    }}
