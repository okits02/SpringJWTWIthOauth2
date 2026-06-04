package com.okits02.SpringJWTWithOauth2.configurations;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI(
            @Value("${server.servlet.context-path}") String contextPath,
            @Value("${spring.application.name}") String appName,
            @Value("${spring.application.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .description("Api for identity using JWT and Oauth2")
                        .version(appVersion)
                        .license(new License().name("Apache 2.0")))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder().group("public").pathsToMatch("/**").build();
    }
}
