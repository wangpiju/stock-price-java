package com.portfolio;

import com.portfolio.service.DatabaseService;
import com.portfolio.service.MarketDataPublisher;
import com.portfolio.service.PortfolioService;

import java.util.ArrayList;

/**
 * The main entry point for the portfolio valuation application.
 * It wires all the components together and starts the simulation.
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Starting Portfolio Valuation System (Java 17)...");

        // 1. Initialize services and load static definitions from the database.
        var dbService = new DatabaseService();
        var productDefinitions = dbService.loadProductDefinitions();

        // 2. Initialize the portfolio service and load the positions from CSV.
        var portfolioService = new PortfolioService(productDefinitions);
        portfolioService.loadPortfolio("src/main/resources/portfolio.csv");

        // 3. Initialize the market data publisher with the initial state of stocks.
        var initialStocks = new ArrayList<>(productDefinitions.stocks().values());
        var marketDataPublisher = new MarketDataPublisher(initialStocks);

        // 4. Register the portfolio service as a listener to market updates.
        marketDataPublisher.addListener(portfolioService);

        // 5. Start the market simulation in a new thread.
        var marketThread = new Thread(marketDataPublisher);
        marketThread.start();

        System.out.println("System running. Market simulation started. Press Ctrl+C to stop.");
    }
}