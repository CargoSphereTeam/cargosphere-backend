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
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void contextLoads() {
        assertThat(routeDefinitionLocator).isNotNull();
    }

    @Test
    void shouldConfigureApplicationAndOpenApiRoutes() {
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
                "shipment-service-route",
                "lb://shipment-service",
                "/api/admin/shipments/**"
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

        assertRoute(
                routes,
                "auth-service-openapi-route",
                "lb://auth-service",
                "/openapi/auth-service"
        );

        assertRoute(
                routes,
                "shipment-service-openapi-route",
                "lb://shipment-service",
                "/openapi/shipment-service"
        );

        assertRoute(
                routes,
                "container-service-openapi-route",
                "lb://container-service",
                "/openapi/container-service"
        );

        assertRoute(
                routes,
                "document-service-openapi-route",
                "lb://document-service",
                "/openapi/document-service"
        );

        assertRoute(
                routes,
                "payment-service-openapi-route",
                "lb://payment-service",
                "/openapi/payment-service"
        );

        assertRoute(
                routes,
                "audit-service-openapi-route",
                "lb://audit-service",
                "/openapi/audit-service"
        );

        assertThat(routes).hasSize(13);
    }

    private void assertRoute(
            Map<String, RouteDefinition> routes,
            String routeId,
            String expectedUri,
            String expectedPath
    ) {
        assertThat(routes).containsKey(routeId);

        RouteDefinition route = routes.get(routeId);

        assertThat(route.getUri())
                .isEqualTo(URI.create(expectedUri));

        boolean pathExists =
                route.getPredicates()
                        .stream()
                        .flatMap(predicate ->
                                predicate
                                        .getArgs()
                                        .values()
                                        .stream()
                        )
                        .anyMatch(expectedPath::equals);

        assertThat(pathExists)
                .as(
                        "Route %s should contain path %s",
                        routeId,
                        expectedPath
                )
                .isTrue();
    }
}
