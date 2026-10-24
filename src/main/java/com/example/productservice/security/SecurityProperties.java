package com.example.productservice.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Credentials supplied to the local OAuth2 authorization server.
 *
 * @param userPassword password for the demonstration user
 * @param adminPassword password for the demonstration administrator
 * @param clientSecret secret used by the registered OAuth2 client
 */
@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(
		@NotBlank String userPassword,
		@NotBlank String adminPassword,
		@NotBlank String clientSecret) {
}
