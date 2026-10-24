package com.example.productservice.product;

import com.example.productservice.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the product API end to end with MockMvc, JPA, Flyway, and H2. */
@SpringBootTest
@AutoConfigureMockMvc
class ProductApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductRepository productRepository;

	@BeforeEach
	void clearProducts() {
		productRepository.deleteAll();
	}

	@Test
	void createsAndReadsAProductThroughTheApiAndDatabase() throws Exception {
		String location = mockMvc.perform(post("/api/products")
				.with(jwt().authorities(
						new SimpleGrantedAuthority("ROLE_USER"),
						new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Keyboard","description":"Mechanical","price":79.99,"stockQuantity":4}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Keyboard"))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		assertThat(productRepository.count()).isEqualTo(1);
		mockMvc.perform(get(location).with(jwt().authorities(
				new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(79.99));
	}
}
