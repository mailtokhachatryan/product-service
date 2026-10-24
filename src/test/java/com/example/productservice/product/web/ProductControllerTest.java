package com.example.productservice.product.web;

import com.example.productservice.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

	private static final String PRODUCT = """
			{"name":"Keyboard","description":"Mechanical","price":79.99,"stockQuantity":4}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductRepository repository;

	@BeforeEach
	void clearProducts() {
		repository.deleteAll();
	}

	@Test
	void createsReadsUpdatesAndDeletesAProduct() throws Exception {
		String location = mockMvc.perform(post("/api/products")
				.with(adminJwt())
				.contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.name").value("Keyboard"))
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get(location).with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(79.99));

		mockMvc.perform(get("/api/products").with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(put(location).with(adminJwt()).contentType(MediaType.APPLICATION_JSON).content("""
				{"name":"Mouse","description":null,"price":25.00,"stockQuantity":12}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Mouse"))
				.andExpect(jsonPath("$.stockQuantity").value(12));

		mockMvc.perform(delete(location).with(adminJwt())).andExpect(status().isNoContent());
		mockMvc.perform(get(location).with(adminJwt())).andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidProductsWithFieldErrors() throws Exception {
		mockMvc.perform(post("/api/products").with(adminJwt()).contentType(MediaType.APPLICATION_JSON).content("""
				{"name":" ","price":-1.00,"stockQuantity":-2}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists())
				.andExpect(jsonPath("$.errors.price").exists())
				.andExpect(jsonPath("$.errors.stockQuantity").exists());
	}

	@Test
	void rejectsMalformedJson() throws Exception {
		mockMvc.perform(post("/api/products").with(adminJwt())
				.contentType(MediaType.APPLICATION_JSON).content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Malformed request body"));
	}

	@Test
	void returnsNotFoundForMissingProducts() throws Exception {
		mockMvc.perform(get("/api/products/999999").with(adminJwt()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
		mockMvc.perform(put("/api/products/999999").with(adminJwt())
				.contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/products/999999").with(adminJwt()))
				.andExpect(status().isNotFound());
	}

	@Test
	void requiresAuthenticationForProducts() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void allowsUsersToReadProducts() throws Exception {
		mockMvc.perform(get("/api/products").with(userJwt()))
				.andExpect(status().isOk());
	}

	@Test
	void rejectsProductMutationsFromUsers() throws Exception {
		mockMvc.perform(post("/api/products").with(userJwt())
				.contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejectsInvalidBearerTokens() throws Exception {
		mockMvc.perform(get("/api/products").header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isUnauthorized());
	}

	private RequestPostProcessor userJwt() {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
	}

	private RequestPostProcessor adminJwt() {
		return jwt().authorities(
				new SimpleGrantedAuthority("ROLE_USER"),
				new SimpleGrantedAuthority("ROLE_ADMIN"));
	}
}
