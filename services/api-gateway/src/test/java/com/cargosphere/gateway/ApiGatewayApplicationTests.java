package com.cargosphere.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ApiGatewayApplication.class,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "app.cors.allowed-origin=http://localhost:5173"
        }
)
class ApiGatewayApplicationTests {

    @Autowired
    private RouteDefinitionLocator
            routeDefinitionLocator;

    @Test
    void contextLoads() {
        assertThat(routeDefinitionLocator)
                .isNotNull();
    }

    @Test
    void shouldConfigureAllRequiredServiceRoutes() {
        Map<String, RouteDefinition> routes =
                routeDefinitionLocator
                        .getRouteDefinitions()
                        .collectList()
                        .blockOptional()
                        .orElseGet(List::of)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        RouteDefinition::getId,
                                        Function.identity()
                                )
                        );

        assertRoute(
                routes,
                "auth-service-route",
                "lb://auth-service",
                "/api/auth/**"
        );

        assertRoute(
                routes,
                "shipment-service-route",
                "lb://shipment-service",
                "/api/shipments/**"
        );

        assertRoute(
                routes,
                "container-types-route",
                "lb://container-service",
                "/api/container-types/**"
        );

        assertRoute(
                routes,
                "container-allocations-route",
                "lb://container-service",
                "/api/container-allocations/**"
        );

        assertRoute(
                routes,
                "document-service-route",
                "lb://document-service",
                "/api/documents/**"
        );

        assertRoute(
                routes,
                "payment-service-route",
                "lb://payment-service",
                "/api/payments/**"
        );

        assertRoute(
                routes,
                "audit-service-route",
                "lb://audit-service",
                "/api/audits/**"
        );

        assertThat(routes).hasSize(7);
    }

    private void assertRoute(
            Map<String, RouteDefinition> routes,
            String routeId,
            String expectedUri,
            String expectedPath
    ) {
        assertThat(routes)
                .containsKey(routeId);

        RouteDefinition route =
                routes.get(routeId);

        assertThat(route.getUri())
                .isEqualTo(
                        URI.create(expectedUri)
                );

        boolean pathExists =
                route.getPredicates()
                        .stream()
                        .flatMap(predicate ->
                                predicate
                                        .getArgs()
                                        .values()
                                        .stream()
                        )
                        .anyMatch(
                                expectedPath::equals
                        );

        assertThat(pathExists)
                .as(
                        "Route %s should contain path %s",
                        routeId,
                        expectedPath
                )
                .isTrue();
    }
}