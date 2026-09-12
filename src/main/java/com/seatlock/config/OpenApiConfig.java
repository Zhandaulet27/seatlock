package com.seatlock.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc auto-generates the OpenAPI spec from the controllers/DTOs
 * already on the classpath - this class only supplies the metadata it
 * can't infer on its own: the top-level Info block, and a named
 * "bearerAuth" security scheme so Swagger UI renders an Authorize button
 * that attaches "Authorization: Bearer <token>" to every request once you
 * paste in a token from POST /api/v1/auth/login.
 * <p>
 * Docs are served at /swagger-ui/index.html (raw JSON at /v3/api-docs) -
 * both are explicitly permitAll'd in SecurityConfig.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SeatLock API",
                version = "v1",
                description = "Concurrency-safe event booking platform. Register and log in "
                        + "via /api/v1/auth, then click Authorize below and paste the returned "
                        + "JWT to try the booking endpoints, which compare the same seat booked "
                        + "through naive, pessimistic-locking, and optimistic-locking strategies."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
