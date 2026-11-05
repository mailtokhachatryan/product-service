package com.example.productservice.product.web;

import java.util.Map;
import java.util.TreeMap;

import com.example.productservice.product.service.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps product API failures to consistent HTTP error responses. */
@RestControllerAdvice(assignableTypes = ProductController.class)
public class ProductExceptionHandler {

	private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";
	private static final String MALFORMED_REQUEST_BODY_MESSAGE = "Malformed request body";

	/**
	 * Returns a 404 response for an unknown product ID.
	 *
	 * @param exception missing-product failure
	 * @return a not-found error response
	 */
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ApiError> notFound(ProductNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiError(HttpStatus.NOT_FOUND.value(), exception.getMessage(), Map.of()));
	}

	/**
	 * Returns a 400 response containing field-validation errors.
	 *
	 * @param exception validation failure
	 * @return a bad-request response with field errors
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> invalidRequest(MethodArgumentNotValidException exception) {
		Map<String, String> errors = new TreeMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.badRequest().body(new ApiError(
				HttpStatus.BAD_REQUEST.value(), VALIDATION_FAILED_MESSAGE, errors));
	}

	/**
	 * Returns a 400 response when the request body cannot be parsed.
	 *
	 * @return a bad-request response for malformed input
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> invalidJson() {
		return ResponseEntity.badRequest().body(new ApiError(
				HttpStatus.BAD_REQUEST.value(), MALFORMED_REQUEST_BODY_MESSAGE, Map.of()));
	}
}
