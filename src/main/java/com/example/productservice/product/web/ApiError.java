package com.example.productservice.product.web;

import java.util.Map;

/**
 * Error response with an HTTP status and optional field-validation messages.
 *
 * @param status HTTP status code
 * @param message summary of the failure
 * @param errors validation messages keyed by field name
 */
public record ApiError(int status, String message, Map<String, String> errors) {
}
