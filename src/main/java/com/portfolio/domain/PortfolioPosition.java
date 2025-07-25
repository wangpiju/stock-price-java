package com.portfolio.domain;

/**
 * An immutable record representing a position in the portfolio.
 * It links a product to a specific quantity.
 */
public record PortfolioPosition(
        Product product,
        int quantity) {
}