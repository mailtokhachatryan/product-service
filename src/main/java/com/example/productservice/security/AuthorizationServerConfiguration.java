package com.example.productservice.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/** Configures OAuth2 user authentication and signed JWT token issuance. */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class AuthorizationServerConfiguration {

	/** Creates the authorization-server configuration. */
	public AuthorizationServerConfiguration() {
	}

	/**
	 * Secures the OAuth2 and OpenID Connect protocol endpoints.
	 *
	 * @param http shared Spring Security configuration
	 * @return the authorization-server filter chain
	 * @throws Exception when the filter chain cannot be built
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.oauth2AuthorizationServer(authorizationServer -> {
					http.securityMatcher(authorizationServer.getEndpointsMatcher());
					authorizationServer.oidc(Customizer.withDefaults());
				})
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
						new LoginUrlAuthenticationEntryPoint("/login"),
						new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
				.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

		return http.build();
	}

	/**
	 * Creates the two demonstration users and their roles.
	 *
	 * @param properties externally supplied credentials
	 * @param passwordEncoder encoder used to hash the passwords
	 * @return the in-memory user store
	 */
	@Bean
	public UserDetailsService userDetailsService(SecurityProperties properties, PasswordEncoder passwordEncoder) {
		UserDetails user = User.withUsername("user")
				.password(passwordEncoder.encode(properties.userPassword()))
				.roles("USER")
				.build();
		UserDetails admin = User.withUsername("admin")
				.password(passwordEncoder.encode(properties.adminPassword()))
				.roles("USER", "ADMIN")
				.build();
		return new InMemoryUserDetailsManager(user, admin);
	}

	/**
	 * Uses Spring Security's adaptive delegating password encoder.
	 *
	 * @return the application password encoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/**
	 * Registers the OAuth2 client used by Postman or another PKCE-capable client.
	 *
	 * @param properties externally supplied client secret
	 * @param passwordEncoder encoder used to hash the client secret
	 * @return the in-memory client registry
	 */
	@Bean
	public RegisteredClientRepository registeredClientRepository(
			SecurityProperties properties, PasswordEncoder passwordEncoder) {
		RegisteredClient productClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("product-client")
				.clientSecret(passwordEncoder.encode(properties.clientSecret()))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("http://127.0.0.1:8081/callback")
				.redirectUri("https://oauth.pstmn.io/v1/callback")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.PROFILE)
				.scope("product.read")
				.scope("product.write")
				.clientSettings(ClientSettings.builder()
						.requireAuthorizationConsent(true)
						.requireProofKey(true)
						.build())
				.tokenSettings(TokenSettings.builder()
						.accessTokenTimeToLive(Duration.ofMinutes(15))
						.refreshTokenTimeToLive(Duration.ofHours(8))
						.reuseRefreshTokens(false)
						.build())
				.build();

		return new InMemoryRegisteredClientRepository(productClient);
	}

	/**
	 * Adds authenticated user roles to JWT access tokens.
	 *
	 * @return the access-token customizer
	 */
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
		return context -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				Authentication principal = context.getPrincipal();
				Collection<? extends GrantedAuthority> authorities = principal == null
						? List.of()
						: principal.getAuthorities();
				Set<String> roles = authorities.stream()
						.filter(Objects::nonNull)
						.map(GrantedAuthority::getAuthority)
						.filter(Objects::nonNull)
						.filter(authority -> authority.startsWith("ROLE_"))
						.map(authority -> authority.substring("ROLE_".length()))
						.collect(Collectors.toUnmodifiableSet());
				context.getClaims().claim("roles", roles);
			}
		};
	}

	/**
	 * Creates an ephemeral RSA signing key for the local product service.
	 *
	 * @return the RSA signing key
	 */
	@Bean
	public RSAKey rsaKey() {
		KeyPair keyPair = generateRsaKey();
		return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate())
				.keyID(UUID.randomUUID().toString())
				.build();
	}

	/**
	 * Publishes the signing key through the authorization server's JWK endpoint.
	 *
	 * @param rsaKey signing key
	 * @return the JWK source used by the token encoder
	 */
	@Bean
	public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
		return new ImmutableJWKSet<>(new JWKSet(rsaKey));
	}

	/**
	 * Creates the decoder used to validate JWT signatures.
	 *
	 * @param rsaKey signing key containing the public verification key
	 * @return the JWT decoder
	 */
	@Bean
	public JwtDecoder jwtDecoder(RSAKey rsaKey) {
		try {
			return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to create the JWT decoder", exception);
		}
	}

	/**
	 * Uses the standard OAuth2 authorization-server endpoint locations.
	 *
	 * @return default authorization-server settings
	 */
	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}

	private KeyPair generateRsaKey() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			return keyPairGenerator.generateKeyPair();
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to generate the JWT signing key", exception);
		}
	}
}
