package com.portfolio.domain;

import java.time.LocalDate;

/**
 * An immutable record representing a European Option's static definition.
 * It contains all necessary information for pricing, such as strike and expiry.
 */
public record EuropeanOption(
        String ticker,
        String underlyingTicker,
        OptionType optionType,
        double strikePrice,
        LocalDate expiryDate) implements Product {
}