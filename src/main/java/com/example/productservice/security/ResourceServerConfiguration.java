package com.example.productservice.security;

import com.example.productservice.product.web.ProductApiPaths;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/** Validates bearer JWTs and applies role-based access rules to the Product API. */
@Configuration(proxyBeanMethods = false)
public class ResourceServerConfiguration {

	private static final String PRODUCT_API_PATH = ProductApiPaths.PRODUCTS_PATH + "/**";
	private static final String LOGIN_PATH = "/login";
	private static final String USER_ROLE = "USER";
	private static final String ADMIN_ROLE = "ADMIN";
	private static final String ROLES_CLAIM = "roles";
	private static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

	/** Creates the resource-server configuration. */
	public ResourceServerConfiguration() {
	}

	/**
	 * Allows users to read products and reserves mutations for administrators.
	 *
	 * @param http shared Spring Security configuration
	 * @return the application filter chain
	 * @throws Exception when the filter chain cannot be built
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain applicationSecurityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
						.requestMatchers(HttpMethod.GET, PRODUCT_API_PATH).hasAnyRole(USER_ROLE, ADMIN_ROLE)
						.requestMatchers(PRODUCT_API_PATH).hasRole(ADMIN_ROLE)
						.requestMatchers(LOGIN_PATH).permitAll()
						.anyRequest().denyAll())
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
				.formLogin(Customizer.withDefaults())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

		return http.build();
	}

	/**
	 * Maps scopes and the custom {@code roles} claim to Spring Security authorities.
	 *
	 * @return the JWT authentication converter
	 */
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter scopeAuthorities = new JwtGrantedAuthoritiesConverter();
		JwtGrantedAuthoritiesConverter roleAuthorities = new JwtGrantedAuthoritiesConverter();
		roleAuthorities.setAuthoritiesClaimName(ROLES_CLAIM);
		roleAuthorities.setAuthorityPrefix(ROLE_AUTHORITY_PREFIX);

		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(
				new DelegatingJwtGrantedAuthoritiesConverter(scopeAuthorities, roleAuthorities));
		return authenticationConverter;
	}
}
