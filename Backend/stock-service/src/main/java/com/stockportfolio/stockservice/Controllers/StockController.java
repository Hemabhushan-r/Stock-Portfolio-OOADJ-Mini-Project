package com.stockportfolio.stockservice.Controllers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.stockportfolio.stockservice.Classes.Response.ApiResponse;
import com.stockportfolio.stockservice.Models.StockOrder;
import com.stockportfolio.stockservice.Services.StockService;

import lombok.AllArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/stock")
public class StockController {
    @Autowired
    private StockService stockService;

    // Command Pattern: Interface for stock operations
    private interface StockOperation {
        double execute(StockService stockService, String ticker);
    }

    // Command Pattern: Concrete implementation for getting stock price
    private static class GetStockPriceOperation implements StockOperation {
        @Override
        public double execute(StockService stockService, String ticker) {
            return stockService.findStockPrice(stockService.findStock(ticker));
        }
    }

    // Command Pattern: Concrete implementation for getting stock high
    private static class GetStockHighOperation implements StockOperation {
        @Override
        public double execute(StockService stockService, String ticker) {
            return stockService.findStockHigh(stockService.findStock(ticker));
        }
    }

    // Facade Pattern: Facade for stock operations
    private double performStockOperation(String ticker, StockOperation operation) {
        return operation.execute(stockService, ticker);
    }

    @PostMapping("/get-stock-price")
    public ResponseEntity<?> getStockPrice(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        double price = performStockOperation(ticker, new GetStockPriceOperation()); // Using Facade
        Map<String, Object> response = new HashMap<>();
        response.put("price", price);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-high")
    public ResponseEntity<?> getStockHigh(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        double price = performStockOperation(ticker, new GetStockHighOperation()); // Using Facade
        Map<String, Object> response = new HashMap<>();
        response.put("high", price);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-low")
    public ResponseEntity<?> getStockLow(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        double price = stockService.findStockLow(stockService.findStock(ticker));
        Map<String, Object> response = new HashMap<>();
        response.put("low", price);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-open")
    public ResponseEntity<?> getStockOpen(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        double price = stockService.findStockOpen(stockService.findStock(ticker));
        Map<String, Object> response = new HashMap<>();
        response.put("open", price);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-prev-close")
    public ResponseEntity<?> getStockPrevClose(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        double price = stockService.findStockPrevClose(stockService.findStock(ticker));
        Map<String, Object> response = new HashMap<>();
        response.put("prevclose", price);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-prev-volume")
    public ResponseEntity<?> getStockVolume(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        double price = stockService.findStockVolume(stockService.findStock(ticker));
        Map<String, Object> response = new HashMap<>();
        response.put("volume", price);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-intraday-history")
    public ResponseEntity<?> getStockIntradayHistory(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        TimeSeriesResponse timeSeriesResponse = stockService.findStockIntradayHistory(ticker);
        Map<String, Object> response = new HashMap<>();
        response.put("metadata", timeSeriesResponse.getMetaData());
        response.put("stockunits", timeSeriesResponse.getStockUnits());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-daily-history")
    public ResponseEntity<?> getStockDailyHistory(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        TimeSeriesResponse timeSeriesResponse = stockService.findStockDailyHistory(ticker);
        Map<String, Object> response = new HashMap<>();
        response.put("metadata", timeSeriesResponse.getMetaData());
        response.put("stockunits", timeSeriesResponse.getStockUnits());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-weekly-history")
    public ResponseEntity<?> getStockWeeklyHistory(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        TimeSeriesResponse timeSeriesResponse = stockService.findStockWeeklyHistory(ticker);
        Map<String, Object> response = new HashMap<>();
        response.put("metadata", timeSeriesResponse.getMetaData());
        response.put("stockunits", timeSeriesResponse.getStockUnits());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-stock-monthly-history")
    public ResponseEntity<?> getStockMonthlyHistory(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        TimeSeriesResponse timeSeriesResponse = stockService.findStockMonthlyHistory(ticker);
        Map<String, Object> response = new HashMap<>();
        response.put("metadata", timeSeriesResponse.getMetaData());
        response.put("stockunits", timeSeriesResponse.getStockUnits());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search-stock")
    public ResponseEntity<?> searchStock(@RequestBody JsonNode request) {
        String keyword = request.get("keyword").asText();
        List<Map<String, Object>> searchResult = stockService.searchByKeyword(keyword);
        Map<String, Object> response = new HashMap<>();
        response.put("bestMatches", searchResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/buy-stock")
    public ResponseEntity<?> buyStock(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        String email = request.get("email").asText();
        StockOrder existingOrder = stockService.findBySymbolAndEmailAndOrderType(ticker, email, "BUY");

        if (existingOrder != null) {
            existingOrder.setQuantity(existingOrder.getQuantity() + 1);
            stockService.createOrUpdateStockOrder(existingOrder);
        } else {
            StockOrder newStockOrder = new StockOrder();
            newStockOrder.setSymbol(ticker);
            newStockOrder.setEmail(email);
            newStockOrder.setQuantity(1); // Set initial quantity to 1
            newStockOrder.setOrderType("BUY"); // Assuming default order type is "BUY"
            stockService.createOrUpdateStockOrder(newStockOrder); // Save the new order
        }
        return ResponseEntity.ok(new ApiResponse(true, "Stock Purchased"));
    }

    @PostMapping("/sell-stock")
    public ResponseEntity<?> sellStock(@RequestBody JsonNode request) {
        String ticker = request.get("ticker").asText();
        String email = request.get("email").asText();
        StockOrder existingOrder = stockService.findBySymbolAndEmailAndOrderType(ticker, email, "BUY");

        if (existingOrder != null) {
            if (existingOrder.getQuantity() - 1 == 0) {
                stockService.deleteStockOrder(existingOrder);
            } else {
                existingOrder.setQuantity(existingOrder.getQuantity() - 1);
                stockService.createOrUpdateStockOrder(existingOrder);
            }
            StockOrder newStockOrder = new StockOrder();
            newStockOrder.setSymbol(ticker);
            newStockOrder.setEmail(email);
            newStockOrder.setQuantity(1); // Set initial quantity to 1
            newStockOrder.setOrderType("SELL"); // Assuming default order type is "BUY"
            stockService.createOrUpdateStockOrder(newStockOrder);
        } else {
            return ResponseEntity.ok(new ApiResponse(false, String.format("Stock of symbol %s not bought", ticker)));// Save
                                                                                                                     // the
                                                                                                                     // new
                                                                                                                     // order
        }
        return ResponseEntity.ok(new ApiResponse(true, "Stock Sold"));
    }

    @PostMapping("/get-stocks-associated-with-email")
    public ResponseEntity<?> getStocksAssociatedWithEmail(@RequestBody JsonNode request) {
        String email = request.get("email").asText();
        List<StockOrder> stockOrders = stockService.findAllByEmail(email);
        if (stockOrders == null) {
            return ResponseEntity.ok(new ApiResponse(false, "No Stocks Asssociated Found"));
        }
        HashMap<String, Object> response = new HashMap<>();
        response.put("results", stockOrders);
        return ResponseEntity.ok(response);
    }
}
