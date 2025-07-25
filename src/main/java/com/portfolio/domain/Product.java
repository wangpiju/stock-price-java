package com.portfolio.domain;

/**
 * A sealed interface representing a financial product.
 * It restricts implementations to a known set of classes (Stock and
 * EuropeanOption),
 * enabling exhaustive checks in pattern matching.
 */
public sealed interface Product permits Stock, EuropeanOption {
    String ticker();
}