package com.talasila.estimate.service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "Estimate API",
        description = "API documentation for the Estimate application. It provides endpoints for user authentication, business management, and more.",
        version = "v1.0"
    ),
    servers = {
        @Server(
            description = "Local Development Server",
            url = "http://localhost:8080"
        )
    },
    security = {
        @SecurityRequirement(name = "jwtCookieAuth")
    }
)
@SecurityScheme(
    name = "jwtCookieAuth",
    description = "JWT token is passed in an HTTP-only cookie. The value for this cookie is automatically set on successful sign-in via the /api/auth/signin endpoint.",
    scheme = "bearer",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    paramName = "jwt-cookie" // IMPORTANT: This must match the 'app.jwt.cookieName' property in your application.properties
)
public class OpenApiConfig {
}