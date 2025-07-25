-- schema.sql

-- Drop tables if they exist to ensure a clean start
DROP TABLE IF EXISTS OPTIONS;
DROP TABLE IF EXISTS STOCKS;

-- Stocks Table to store static definitions of common stocks
CREATE TABLE STOCKS (
    ticker VARCHAR(20) PRIMARY KEY,
    company_name VARCHAR(255),
    initial_price DOUBLE,
    mu DOUBLE,  -- Expected return for Brownian motion model
    sigma DOUBLE -- Volatility for Brownian motion model
);

-- Options Table to store static definitions of European options
CREATE TABLE OPTIONS (
    ticker VARCHAR(50) PRIMARY KEY,
    underlying_ticker VARCHAR(20) NOT NULL,
    option_type VARCHAR(4) NOT NULL, -- 'CALL' or 'PUT'
    strike_price DOUBLE NOT NULL,
    expiry_date DATE NOT NULL,
    FOREIGN KEY (underlying_ticker) REFERENCES STOCKS(ticker)
);

-- Insert static data for the securities we support
-- Note: The current date is July 25, 2025.
INSERT INTO STOCKS (ticker, company_name, initial_price, mu, sigma) VALUES
('AAPL', 'Apple Inc.', 150.00, 0.15, 0.30),
('TSLA', 'Tesla Inc.', 400.00, 0.35, 0.60);

INSERT INTO OPTIONS (ticker, underlying_ticker, option_type, strike_price, expiry_date) VALUES
('AAPL-OCT-2025-110-C', 'AAPL', 'CALL', 110.0, '2025-10-17'),
('TSLA-NOV-2025-400-C', 'TSLA', 'CALL', 400.0, '2025-11-21'),
('TSLA-DEC-2025-450-P', 'TSLA', 'PUT', 450.0, '2025-12-19');