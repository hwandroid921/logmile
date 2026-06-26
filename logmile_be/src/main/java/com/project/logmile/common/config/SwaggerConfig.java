package com.project.logmile.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "Bearer";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
            .components(new Components()
                .addSecuritySchemes(BEARER_SCHEME, bearerSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
            .title("logmile API")
            .description("""
                ## 화물차 운전자 피로도 실시간 모니터링 플랫폼

                ### 인증 방법
                1. `/api/auth/login` 으로 JWT 토큰 발급
                2. 우측 상단 **Authorize** 버튼 클릭
                3. 발급받은 토큰 입력 (`Bearer` 없이 토큰만 입력)

                ### 권한 구조
                - `ROLE_SUPER_ADMIN` : 최상위 관리자 — 승인/거절/정지, 업체 목록 조회
                - `ROLE_ADMIN` : 일반 관리자 — 대시보드, 차량/운전자 관리, 운행 이력 (소속 업체 데이터만)
                """)
            .version("v1.0")
            .contact(new Contact()
                .name("logmile 개발팀")
                .email("olgksgml@gmail.com"));
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization")
            .description("JWT 토큰을 입력하세요. (Bearer 접두사 제외)");
    }
}
