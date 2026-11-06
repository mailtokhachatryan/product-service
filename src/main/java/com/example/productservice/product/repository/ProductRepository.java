package com.example.productservice.product.repository;

import com.example.productservice.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides Spring Data persistence operations for products. */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
