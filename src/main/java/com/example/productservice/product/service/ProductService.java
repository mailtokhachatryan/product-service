package com.example.productservice.product.service;

import java.util.List;

import com.example.productservice.monitoring.ProductMetrics;
import com.example.productservice.product.Product;
import com.example.productservice.product.dto.ProductRequest;
import com.example.productservice.product.dto.ProductResponse;
import com.example.productservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implements product operations and transaction boundaries. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductMetrics productMetrics;

	/**
	 * Persists a new product.
	 *
	 * @param request validated product details
	 * @return the created product with its database ID
	 */
	@Transactional
	public ProductResponse create(ProductRequest request) {
		Product product = new Product(request.name(), request.description(), request.price(), request.stockQuantity());
		ProductResponse response = toResponse(productRepository.save(product));
		productMetrics.recordCreated();
		return response;
	}

	/**
	 * Returns all products in ascending ID order.
	 *
	 * @return the ordered product list
	 */
	public List<ProductResponse> findAll() {
		return productRepository.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
	}

	/**
	 * Finds a product, throwing {@link ProductNotFoundException} when absent.
	 *
	 * @param id product ID
	 * @return the matching product
	 */
	public ProductResponse findById(long id) {
		return toResponse(findProduct(id));
	}

	/**
	 * Updates a product within a transaction so JPA persists the changed fields.
	 *
	 * @param id product ID
	 * @param request validated replacement details
	 * @return the updated product
	 */
	@Transactional
	public ProductResponse update(long id, ProductRequest request) {
		Product product = findProduct(id);
		product.setName(request.name());
		product.setDescription(request.description());
		product.setPrice(request.price());
		product.setStockQuantity(request.stockQuantity());
		return toResponse(product);
	}

	/**
	 * Deletes an existing product, throwing when no product has the given ID.
	 *
	 * @param id product ID
	 */
	@Transactional
	public void delete(long id) {
		productRepository.delete(findProduct(id));
		productMetrics.recordDeleted();
	}

	private Product findProduct(long id) {
		return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
	}

	private ProductResponse toResponse(Product product) {
		return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
				product.getPrice(), product.getStockQuantity());
	}
}
