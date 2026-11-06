package com.example.productservice.product.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.example.productservice.monitoring.ProductMetrics;
import com.example.productservice.product.Product;
import com.example.productservice.product.dto.ProductRequest;
import com.example.productservice.product.dto.ProductResponse;
import com.example.productservice.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies product business logic without starting the Spring application context. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	private static final ProductRequest REQUEST = new ProductRequest(
			"Keyboard", "Mechanical", new BigDecimal("79.99"), 4);

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductMetrics productMetrics;

	@InjectMocks
	private ProductService productService;

	@Test
	void createsAProductAndRecordsTheMetric() {
		Product product = new Product(REQUEST.name(), REQUEST.description(), REQUEST.price(), REQUEST.stockQuantity());
		when(productRepository.save(any(Product.class))).thenReturn(product);

		ProductResponse response = productService.create(REQUEST);

		assertThat(response.name()).isEqualTo("Keyboard");
		assertThat(response.price()).isEqualByComparingTo("79.99");
		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productRepository).save(productCaptor.capture());
		assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(4);
		verify(productMetrics).recordCreated();
	}

	@Test
	void updatesAnExistingProduct() {
		Product product = new Product("Keyboard", "Mechanical", new BigDecimal("79.99"), 4);
		when(productRepository.findById(7L)).thenReturn(Optional.of(product));

		ProductResponse response = productService.update(7L,
				new ProductRequest("Mouse", "Wireless", new BigDecimal("25.00"), 12));

		assertThat(response.name()).isEqualTo("Mouse");
		assertThat(response.description()).isEqualTo("Wireless");
		assertThat(response.stockQuantity()).isEqualTo(12);
	}

	@Test
	void rejectsUnknownProducts() {
		when(productRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.findById(404L))
				.isInstanceOf(ProductNotFoundException.class)
				.hasMessage("Product 404 was not found");
		verify(productRepository, never()).delete(any(Product.class));
	}

	@Test
	void deletesAnExistingProductAndRecordsTheMetric() {
		Product product = new Product("Mouse", "Wireless", new BigDecimal("25.00"), 12);
		when(productRepository.findById(7L)).thenReturn(Optional.of(product));

		productService.delete(7L);

		verify(productRepository).delete(product);
		verify(productMetrics).recordDeleted();
	}
}
