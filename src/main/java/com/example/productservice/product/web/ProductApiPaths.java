package com.example.productservice.product.web;

/** Shared paths used by the Product REST API and its security rules. */
public final class ProductApiPaths {

	/** Collection endpoint for products. */
	public static final String PRODUCTS_PATH = "/api/products";

	/** Item endpoint for a product ID. */
	public static final String PRODUCT_ID_PATH = "/{id}";

	/** Prefix used when building a Location header for a product resource. */
	public static final String PRODUCT_RESOURCE_PATH_PREFIX = PRODUCTS_PATH + "/";

	private ProductApiPaths() {
	}
}
