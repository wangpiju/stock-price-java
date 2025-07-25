package com.portfolio.service;

import com.portfolio.domain.Stock;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A publisher that simulates real-time stock price changes.
 * It runs in a separate thread and publishes immutable lists of updated stocks.
 */
public class MarketDataPublisher implements Runnable {
    // AtomicReference safely holds the current, immutable list of stocks for
    // thread-safe updates.
    private final AtomicReference<List<Stock>> currentStocks;
    private final Random random = new Random();
    // A thread-safe list for listeners.
    private final List<PortfolioService> listeners = new CopyOnWriteArrayList<>();

    // Constants for the Geometric Brownian Motion model from the appendix.
    private static final double T_SECONDS = 7257600.0;
    private static final double DELTA_T_SECONDS = 1.0;

    public MarketDataPublisher(List<Stock> initialStocks) {
        this.currentStocks = new AtomicReference<>(initialStocks);
    }

    public void addListener(PortfolioService listener) {
        listeners.add(listener);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Generate a new list of stocks with updated prices (immutable update).
                List<Stock> newStockList = currentStocks.get().stream()
                        .map(this::calculateNewPrice)
                        .toList(); // Java 16+ Stream.toList() returns an unmodifiable list.

                // Atomically set the new state.
                currentStocks.set(newStockList);

                // Notify all listeners with the new immutable state.
                for (var listener : listeners) {
                    listener.onMarketUpdate(newStockList);
                }

                Thread.sleep(1000); // Wait for 1 second before the next update.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Market data publisher was interrupted.");
            }
        }
    }

    /**
     * Calculates the next price for a stock using the Geometric Brownian Motion
     * model.
     * 
     * @param oldStock The stock's previous state.
     * @return A new Stock record instance with the updated price.
     */
    private Stock calculateNewPrice(Stock oldStock) {
        double s = oldStock.currentPrice();
        double mu = oldStock.mu();
        double sigma = oldStock.sigma();
        double epsilon = random.nextGaussian(); // A random variable from the standard normal distribution.

        double deltaS = s
                * (mu * (DELTA_T_SECONDS / T_SECONDS) + sigma * epsilon * Math.sqrt(DELTA_T_SECONDS / T_SECONDS));
        double newPrice = s + deltaS;

        // Clamp the price to the range [0.5, 1000.0] as per a potential interpretation
        // of the problem statement.
        newPrice = Math.max(0.5, Math.min(newPrice, 1000.0));

        // Return a new Stock record instance, preserving immutability.
        return new Stock(oldStock.ticker(), oldStock.companyName(), newPrice, mu, sigma);
    }
}