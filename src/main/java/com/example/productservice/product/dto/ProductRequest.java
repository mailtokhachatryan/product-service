package com.example.productservice.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Validated input for creating or replacing a product.
 *
 * @param name nonblank product name
 * @param description optional product description
 * @param price nonnegative price with at most two decimal places
 * @param stockQuantity nonnegative number of units in stock
 */
public record ProductRequest(
		@NotBlank @Size(max = 150) String name,
		@Size(max = 1000) String description,
		@NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
		@NotNull @Min(0) Integer stockQuantity) {
}
