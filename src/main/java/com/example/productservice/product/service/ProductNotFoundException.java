package com.example.productservice.product.service;

/** Signals that a requested product ID does not exist. */
public class ProductNotFoundException extends RuntimeException {

	/**
	 * Creates an exception for the missing product ID.
	 *
	 * @param id missing product ID
	 */
	public ProductNotFoundException(long id) {
		super("Product " + id + " was not found");
	}
}
