package com.portfolio.util;

import com.portfolio.domain.EuropeanOption;
import com.portfolio.domain.OptionType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A final utility class for calculating option prices using the Black-Scholes
 * model.
 * It cannot be instantiated or extended.
 */
public final class BlackScholesCalculator {
    // A constant for the risk-free interest rate, assumed to be 2%.
    private static final double RISK_FREE_RATE = 0.02;

    // Private constructor to prevent instantiation of this utility class.
    private BlackScholesCalculator() {
    }

    /**
     * Calculates the price of a European option.
     * 
     * @param option            The option to price.
     * @param currentStockPrice The current price of the underlying stock.
     * @param volatility        The volatility of the underlying stock.
     * @return The calculated theoretical price of the option.
     */
    public static double calculate(EuropeanOption option, double currentStockPrice, double volatility) {
        var strike = option.strikePrice();
        var today = LocalDate.now();
        var expiry = option.expiryDate();
        double timeToExpiryYears = (double) ChronoUnit.DAYS.between(today, expiry) / 365.0;

        // If the option has expired, calculate its intrinsic value.
        if (timeToExpiryYears <= 0) {
            return Math.max(0,
                    option.optionType() == OptionType.CALL ? currentStockPrice - strike : strike - currentStockPrice);
        }

        // Black-Scholes formula components
        double d1 = (Math.log(currentStockPrice / strike)
                + (RISK_FREE_RATE + 0.5 * Math.pow(volatility, 2)) * timeToExpiryYears)
                / (volatility * Math.sqrt(timeToExpiryYears));
        double d2 = d1 - volatility * Math.sqrt(timeToExpiryYears);

        if (option.optionType() == OptionType.CALL) {
            return currentStockPrice * cdf(d1) - strike * Math.exp(-RISK_FREE_RATE * timeToExpiryYears) * cdf(d2);
        } else { // PUT
            return strike * Math.exp(-RISK_FREE_RATE * timeToExpiryYears) * cdf(-d2) - currentStockPrice * cdf(-d1);
        }
    }

    /**
     * Cumulative Distribution Function (CDF) for the standard normal distribution.
     * Uses the Abramowitz and Stegun approximation, a common and accurate method.
     * 
     * @param z The value for which to calculate the CDF.
     * @return The probability P(X <= z) for a standard normal variable X.
     */
    public static double cdf(double z) {
        if (z < -8.0)
            return 0.0;
        if (z > 8.0)
            return 1.0;

        double b1 = 0.319381530, b2 = -0.356563782, b3 = 1.781477937, b4 = -1.821255978, b5 = 1.330274429;
        double p = 0.2316419, c2 = 0.39894228;

        var a = Math.abs(z);
        var t = 1.0 / (1.0 + a * p);
        var b = c2 * Math.exp((-z) * (z / 2.0));
        var n = ((((b5 * t + b4) * t + b3) * t + b2) * t + b1) * t;
        n = 1.0 - b * n;

        if (z < 0.0) {
            n = 1.0 - n;
        }
        return n;
    }
}