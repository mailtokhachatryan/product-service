package com.example.productservice.product.repository;

import java.math.BigDecimal;

import com.example.productservice.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies product persistence against the in-memory test database. */
@SpringBootTest
@Transactional
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	@Test
	void savesAndLoadsAProduct() {
		Product product = new Product("Keyboard", "Mechanical", new BigDecimal("79.99"), 4);

		Product saved = productRepository.saveAndFlush(product);

		assertThat(saved.getId()).isNotNull();
		assertThat(productRepository.findById(saved.getId()))
				.get()
				.extracting(Product::getName, Product::getDescription, Product::getPrice, Product::getStockQuantity)
				.containsExactly("Keyboard", "Mechanical", new BigDecimal("79.99"), 4);
	}

	@Test
	void deletesAProduct() {
		Product saved = productRepository.saveAndFlush(
				new Product("Mouse", "Wireless", new BigDecimal("25.00"), 12));

		productRepository.deleteById(saved.getId());
		productRepository.flush();

		assertThat(productRepository.findById(saved.getId())).isEmpty();
	}
}
