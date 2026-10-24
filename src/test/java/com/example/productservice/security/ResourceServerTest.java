package com.example.productservice.security;

import java.time.Instant;
import java.util.List;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceServerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JWKSource<SecurityContext> jwkSource;

	@Test
	void acceptsAValidSignedJwtWithTheUserRole() throws Exception {
		mockMvc.perform(get("/api/products").header("Authorization", "Bearer " + userAccessToken()))
				.andExpect(status().isOk());
	}

	private String userAccessToken() {
		Instant issuedAt = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject("user")
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(300))
				.claim("scope", "product.read")
				.claim("roles", List.of("USER"))
				.build();
		JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}
