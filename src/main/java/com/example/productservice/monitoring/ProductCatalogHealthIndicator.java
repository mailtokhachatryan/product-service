package com.example.productservice.monitoring;

import com.example.productservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/** Reports whether the product repository is available and includes its current row count. */
@Component
@RequiredArgsConstructor
public class ProductCatalogHealthIndicator implements HealthIndicator {

	private final ProductRepository productRepository;

	@Override
	public Health health() {
		try {
			long productCount = productRepository.count();
			return Health.up().withDetail("productCount", productCount).build();
		}
		catch (DataAccessException exception) {
			return Health.down(exception).withDetail("reason", "Product repository is unavailable").build();
		}
	}
}
