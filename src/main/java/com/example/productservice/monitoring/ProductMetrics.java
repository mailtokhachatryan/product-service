package com.example.productservice.monitoring;

import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/** Registers product-operation metrics for Actuator and Prometheus consumers. */
@Component
public class ProductMetrics {

	private final Counter createdProducts;
	private final Counter deletedProducts;
	private final AtomicLong currentProductCount = new AtomicLong();

	/**
	 * Creates the product metrics in the shared Micrometer registry.
	 *
	 * @param meterRegistry application meter registry
	 */
	public ProductMetrics(MeterRegistry meterRegistry) {
		createdProducts = Counter.builder("product.service.products.created")
				.description("Number of products created through the service")
				.register(meterRegistry);
		deletedProducts = Counter.builder("product.service.products.deleted")
				.description("Number of products deleted through the service")
				.register(meterRegistry);
		Gauge.builder("product.service.products.current", currentProductCount, AtomicLong::doubleValue)
				.description("Number of products created minus deleted through the service")
				.register(meterRegistry);
	}

	/** Records a successful product creation. */
	public void recordCreated() {
		createdProducts.increment();
		currentProductCount.incrementAndGet();
	}

	/** Records a successful product deletion. */
	public void recordDeleted() {
		deletedProducts.increment();
		currentProductCount.decrementAndGet();
	}
}
