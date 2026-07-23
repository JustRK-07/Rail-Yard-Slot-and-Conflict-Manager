package com.justrk07.railyard.common.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI railYardOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Rail Yard Slot and Conflict Manager API")
                .description("Conflict-safe scheduling services for the rail yard portfolio project.")
                .version("0.0.1-SNAPSHOT")
                .license(new License().name("Portfolio demo only")));
    }
}
