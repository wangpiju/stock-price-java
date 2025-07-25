package com.portfolio.domain;

/**
 * An immutable record representing a Stock.
 * It holds both static definition data (mu, sigma) and dynamic data
 * (currentPrice).
 * In our immutable design, a price change results in a new Stock record
 * instance.
 */
public record Stock(
        String ticker,
        String companyName,
        double currentPrice,
        double mu,
        double sigma) implements Product {
}