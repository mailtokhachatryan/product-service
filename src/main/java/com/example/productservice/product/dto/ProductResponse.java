package com.example.productservice.product.dto;

import java.math.BigDecimal;

/**
 * Product representation returned by the REST API.
 *
 * @param id database-generated product ID
 * @param name product name
 * @param description optional product description
 * @param price product price
 * @param stockQuantity number of units in stock
 */
public record ProductResponse(Long id, String name, String description, BigDecimal price, int stockQuantity) {
}
