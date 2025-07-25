# Real-Time Portfolio Valuation System

## 1. Overview

This project is a Java-based simulation system designed to calculate and display the real-time value of a financial portfolio. The portfolio consists of common stocks, European call options, and European put options.

The system simulates live market conditions by having stock prices fluctuate according to the Discrete Time Geometric Brownian Motion model. Concurrently, it re-calculates the theoretical prices of options using the Black-Scholes model and continuously publishes the updated market value of each position and the portfolio's total Net Asset Value (NAV) to the console.

## 2. Features

* **Portfolio Loading:** Loads initial positions from a mock CSV file.
* **Database Integration:** Retrieves static security definitions (e.g., strike price, maturity) from an embedded H2 database.
* **Real-time Market Simulation:** A dedicated publisher runs in a separate thread to simulate stock price changes every 0.5 to 2 seconds.
* **Stochastic Price Model:** Stock prices evolve according to the Geometric Brownian Motion financial model.
* **Advanced Option Pricing:** European option values are calculated in real-time using the industry-standard Black-Scholes formula.
* **Live Value Reporting:** A subscriber listens for market updates and prints a formatted report of each position's market value and the total portfolio NAV to the console.
* **Modern Java Implementation:** Built using Java 17, leveraging modern features like Records and Sealed Interfaces for creating robust and immutable data models.

## 3. Tech Stack & Requirements

* **Language:** Java 17 (Upgraded from the original JDK 1.8 specification for modern features).
* **Build Tool:** Gradle.
* **Database:** H2 In-Memory Database (as per the allowance for H2 or SQLite).
* **Dependencies:** `com.h2database:h2:2.2.224` (This is the only third-party library used, as allowed by the project constraints).

## 4. Setup and Installation

* **Prerequisites:**
  * Ensure you have JDK 17 or higher installed.
  * Ensure you have Gradle installed and configured in your system's PATH.
* **File Structure:**
  * Place the entire project source code in a directory.
  * Ensure the following data files are located in `src/main/resources/`:
    * `portfolio.csv` (Defines initial positions).
    * `schema.sql` (Defines the database schema and static security data).

## 5. How to Run

1. **Open Terminal:** Open your terminal or command prompt.
2. **Navigate to Project Root:** Use the `cd` command to navigate to the project's root directory (the folder containing the `build.gradle` file).
3. **Execute the Run Command:** Use the following Gradle command. The `clean` task is recommended to avoid potential caching issues.

    ```bash
    gradle clean run
    ```

4. **Observe the Output:** The application will start, and you will see real-time portfolio updates printed to the console every second.
5. **Stop the Application:** Press `Ctrl + C` in the terminal to stop the simulation.

## 6. Project Structure

```
.
├── build.gradle                # Gradle build script
└── src
    └── main
        ├── java
        │   └── com
        │       └── portfolio
        │           ├── App.java          # Application entry point
        │           ├── domain            # Data models (Stock, EuropeanOption, etc.)
        │           ├── service           # Business logic (PortfolioService, MarketDataPublisher, etc.)
        │           └── util              # Utility classes (BlackScholesCalculator)
        └── resources
            ├── portfolio.csv     # Initial portfolio positions
            └── schema.sql        # Database schema and seed data
```

* **domain:** Contains the core data models for stocks and options. Implemented as immutable Java 17 `records` for thread safety and conciseness.
* **service:** Holds the main business logic, including the market data publisher and portfolio subscriber.
* **util:** Contains helper classes, most notably the `BlackScholesCalculator`.
* **resources:** Contains all non-code files required by the application, including the position file and database schema.

## 7. Architecture & Design

The system is designed around a clean, decoupled **Publisher-Subscriber** architectural pattern.

* **Publisher (MarketDataPublisher):**
  * This component's sole responsibility is to generate and publish market data.
  * It runs in a separate thread to simulate asynchronous data feeds without blocking the main application.
  * In each cycle, it calculates new stock prices using the Geometric Brownian Motion formula and publishes an immutable list of the new market state to all subscribers. Each stock's expected return (μ) and volatility (σ) are treated as static fields.
* **Subscriber (PortfolioService):**
  * This component acts as the portfolio result subscriber. It listens for updates from the publisher.
  * Upon receiving new market data, it triggers the re-valuation logic.
  * It iterates through each portfolio position, calculating its current market value. Stock values are calculated directly (price * shares). Option values are calculated using their theoretical price from the Black-Scholes formula.
  * Finally, it aggregates all values to compute the total portfolio NAV and prints the results to the console.
* **Immutability:**
  * A core design principle of this project is immutability. All domain objects (`Stock`, `EuropeanOption`, `PortfolioPosition`) are implemented as Java 17 `records`.
  * This means that data objects are never modified. Instead, when a stock price changes, a *new* `Stock` object is created. This design greatly simplifies development in a multi-threaded environment by eliminating entire classes of concurrency bugs.
