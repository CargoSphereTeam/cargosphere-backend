package com.cargosphere.registry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = ServiceRegistryApplication.class,
        properties = {
                "spring.autoconfigure.exclude="
                        + "org.springframework.cloud.netflix.eureka.server."
                        + "EurekaServerAutoConfiguration"
        }
)
class ServiceRegistryApplicationTests {

    @Test
    void contextLoads() {
    }
}