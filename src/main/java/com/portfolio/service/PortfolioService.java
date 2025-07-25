package com.portfolio.service;

import com.portfolio.domain.*;
import com.portfolio.util.BlackScholesCalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages the portfolio, loads positions, and acts as a listener for market
 * data updates.
 * It recalculates and prints the portfolio's value in real-time.
 */
public class PortfolioService {

    private final List<PortfolioPosition> positions = new ArrayList<>();
    private final Map<String, Product> productDefinitions;
    private static final AtomicInteger updateCount = new AtomicInteger(0);

    public PortfolioService(DatabaseService.ProductDefinitions definitions) {
        this.productDefinitions = new java.util.HashMap<>();
        this.productDefinitions.putAll(definitions.stocks());
        this.productDefinitions.putAll(definitions.options());
    }

    /**
     * Loads portfolio positions from a CSV file.
     * * @param csvFilePath The path to the portfolio CSV file.
     */
    public void loadPortfolio(String csvFilePath) {
        // Use try-with-resources for the reader.
        try (var br = new BufferedReader(new FileReader(csvFilePath))) {
            br.readLine(); // Skip header line.
            String line;
            while ((line = br.readLine()) != null) {
                var values = line.split(",");
                var ticker = values[0];
                var quantity = Integer.parseInt(values[1]);

                var product = productDefinitions.get(ticker);
                if (product != null) {
                    positions.add(new PortfolioPosition(product, quantity));
                }
            }
            System.out.println("Portfolio loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener method called by the MarketDataPublisher on each market update.
     * * @param updatedStocks The new, immutable list of stocks with their latest
     * prices.
     */
    public void onMarketUpdate(List<Stock> updatedStocks) {
        // Create an efficient lookup map for the latest stock prices.
        Map<String, Stock> stockPriceMap = updatedStocks.stream()
                .collect(Collectors.toUnmodifiableMap(Stock::ticker, Function.identity()));

        printPortfolio(stockPriceMap);
    }

    /**
     * Calculates the current value of all positions and prints a formatted report.
     * * @param currentStockPrices A map containing the latest prices for all
     * stocks.
     */
    private void printPortfolio(Map<String, Stock> currentStockPrices) {
        // Use a Text Block for clean, multi-line string formatting.
        System.out.printf("""

                ----------------------------------------------------------------------
                ## %d Market Data Update
                AAPL change to %.2f
                TSLA change to %.2f
                ## Portfolio
                %-25s %10s %15s %15s
                ----------------------------------------------------------------------
                """,
                updateCount.incrementAndGet(),
                currentStockPrices.get("AAPL").currentPrice(),
                currentStockPrices.get("TSLA").currentPrice(),
                "symbol", "price", "qty", "value");

        double totalNav = 0;
        for (var pos : positions) {
            double price = 0;
            double value = 0;

            // Use Pattern Matching for instanceof for cleaner, safer type checking and
            // casting.
            if (pos.product() instanceof Stock stock) {
                price = currentStockPrices.get(stock.ticker()).currentPrice();
                value = price * pos.quantity();
            } else if (pos.product() instanceof EuropeanOption option) {
                Stock underlying = currentStockPrices.get(option.underlyingTicker());
                // For options, volatility from the underlying stock's definition is used.
                price = BlackScholesCalculator.calculate(option, underlying.currentPrice(), underlying.sigma());
                // Option value = theoretical price * quantity * contract size (usually 100).
                value = price * pos.quantity() * 100;
            }

            // ==================== FINAL SOLUTION ====================
            // This method avoids the complex printf bug in your environment.
            // Step 1: Format each number into a string individually using a safe locale.
            String tickerStr = pos.product().ticker();
            String priceStr = String.format(java.util.Locale.US, "%.2f", price);
            String qtyStr = String.format(java.util.Locale.US, "%,d",
                    pos.product() instanceof EuropeanOption ? pos.quantity() * 100 : pos.quantity());
            String valueStr = String.format(java.util.Locale.US, "%,.2f", value);

            // Step 2: Use the simplest possible printf with only %s (string) to handle
            // padding and alignment.
            String finalOutput = String.format("%-25s %10s %15s %15s",
                    tickerStr,
                    priceStr,
                    qtyStr,
                    valueStr);

            System.out.println(finalOutput);
            // =========================================================

            totalNav += value;
        }

        System.out.printf(java.util.Locale.US, """
                ----------------------------------------------------------------------
                Total portfolio value: %,.2f
                ----------------------------------------------------------------------
                """, totalNav);
    }
}