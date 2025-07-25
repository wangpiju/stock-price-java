package com.portfolio.service;

import com.portfolio.domain.*;
import org.h2.tools.RunScript;
import java.io.FileReader;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to manage database interactions.
 * It initializes an in-memory H2 database and loads product definitions from
 * it.
 */
public class DatabaseService {
    private static final String DB_URL = "jdbc:h2:mem:portfolio;DB_CLOSE_DELAY=-1";
    private static final String SCHEMA_PATH = "src/main/resources/schema.sql";

    /**
     * A record to hold the loaded product definitions, separating stocks and
     * options.
     */
    public record ProductDefinitions(Map<String, Stock> stocks, Map<String, EuropeanOption> options) {
    }

    /**
     * Initializes the database and loads all product definitions.
     * 
     * @return A ProductDefinitions record containing maps of all stocks and
     *         options.
     */
    public ProductDefinitions loadProductDefinitions() {
        var stocks = new HashMap<String, Stock>();
        var options = new HashMap<String, EuropeanOption>();

        // Use try-with-resources for automatic resource management
        try (var connection = DriverManager.getConnection(DB_URL);
                var reader = new FileReader(SCHEMA_PATH)) {

            RunScript.execute(connection, reader);
            System.out.println("Database initialized and schema loaded.");

            try (var stmt = connection.createStatement()) {
                // Load Stocks from the database
                var rs = stmt.executeQuery("SELECT * FROM STOCKS");
                while (rs.next()) {
                    var stock = new Stock(
                            rs.getString("ticker"),
                            rs.getString("company_name"),
                            rs.getDouble("initial_price"),
                            rs.getDouble("mu"),
                            rs.getDouble("sigma"));
                    stocks.put(stock.ticker(), stock);
                }

                // Load Options from the database
                rs = stmt.executeQuery("SELECT * FROM OPTIONS");
                while (rs.next()) {
                    var option = new EuropeanOption(
                            rs.getString("ticker"),
                            rs.getString("underlying_ticker"),
                            OptionType.valueOf(rs.getString("option_type")),
                            rs.getDouble("strike_price"),
                            rs.getDate("expiry_date").toLocalDate());
                    options.put(option.ticker(), option);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize or load from database", e);
        }
        return new ProductDefinitions(stocks, options);
    }
}