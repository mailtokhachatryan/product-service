package com.example.productservice.monitoring;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the Actuator health and custom metrics endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void exposesProductCatalogHealth() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components.productCatalog.status").value("UP"))
				.andExpect(jsonPath("$.components.productCatalog.details.productCount").value(0));
	}

	@Test
	void exposesCustomMetricsToAdministrators() throws Exception {
		mockMvc.perform(get("/actuator/metrics/product.service.products.created").with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("product.service.products.created"));
	}

	@Test
	void exposesPrometheusMetricsToAdministrators() throws Exception {
		mockMvc.perform(get("/actuator/prometheus").with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("product_service_products_total")));
	}

	@Test
	void protectsOperationalMetricsFromUnauthenticatedRequests() throws Exception {
		mockMvc.perform(get("/actuator/metrics"))
				.andExpect(status().isUnauthorized());
	}

	private RequestPostProcessor adminJwt() {
		return jwt().authorities(
				new SimpleGrantedAuthority("ROLE_USER"),
				new SimpleGrantedAuthority("ROLE_ADMIN"));
	}
}
