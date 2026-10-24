package com.example.productservice.product.web;

import java.net.URI;
import java.util.List;

import com.example.productservice.product.dto.ProductRequest;
import com.example.productservice.product.dto.ProductResponse;
import com.example.productservice.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes CRUD operations for products over HTTP. */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	/**
	 * Creates a product and returns the URI of the new resource.
	 *
	 * @param request validated product details
	 * @return the created product and its resource URI
	 */
	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
		ProductResponse product = productService.create(request);
		return ResponseEntity.created(URI.create("/api/products/" + product.id())).body(product);
	}

	/**
	 * Returns all products ordered by ID.
	 *
	 * @return the ordered product list
	 */
	@GetMapping
	public List<ProductResponse> findAll() {
		return productService.findAll();
	}

	/**
	 * Returns the product identified by {@code id}.
	 *
	 * @param id product ID
	 * @return the matching product
	 */
	@GetMapping("/{id}")
	public ProductResponse findById(@PathVariable long id) {
		return productService.findById(id);
	}

	/**
	 * Replaces the editable fields of the product identified by {@code id}.
	 *
	 * @param id product ID
	 * @param request validated replacement details
	 * @return the updated product
	 */
	@PutMapping("/{id}")
	public ProductResponse update(@PathVariable long id, @Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	/**
	 * Deletes the product identified by {@code id}.
	 *
	 * @param id product ID
	 * @return an empty 204 response
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
