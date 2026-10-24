package com.example.productservice.product;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Persisted product with an ID managed by the database. */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	@Setter
	private String name;

	@Column(length = 1000)
	@Setter
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	@Setter
	private BigDecimal price;

	@Column(nullable = false)
	@Setter
	private int stockQuantity;

	/**
	 * Creates a product before it receives a database ID.
	 *
	 * @param name product name
	 * @param description optional description
	 * @param price product price
	 * @param stockQuantity units in stock
	 */
	public Product(String name, String description, BigDecimal price, int stockQuantity) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.stockQuantity = stockQuantity;
	}

}
