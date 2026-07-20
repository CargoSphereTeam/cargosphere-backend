package com.cargosphere.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = ApiGatewayApplication.class,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}