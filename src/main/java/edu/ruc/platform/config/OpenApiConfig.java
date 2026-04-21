package edu.ruc.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_BEARER = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("学院学生综合服务与党团管理平台 API")
                        .version("v1.0.0")
                        .description("面向学生端小程序与管理员端后台的统一后端服务"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_BEARER))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
