package com.example.backend.service.fraud;

import com.example.backend.model.Purchase;
import com.example.backend.model.HistoricalAnalysis;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The TransactionHistoryAnalyzer class is responsible for analyzing a user's transaction history
 * to detect patterns and anomalies. It uses an IgniteCache to store and retrieve user transaction data.
 * The analysis includes calculating statistical measures such as average amount and standard deviation,
 * identifying common merchants, typical purchase hours, purchase frequency, and detecting unusual patterns.
 *
 * @component Indicates that this class is a Spring component.
 */
@Component
public class TransactionHistoryAnalyzer {
    private final IgniteCache<Long, List<Purchase>> userTransactionCache;

    public TransactionHistoryAnalyzer(IgniteCache<Long, List<Purchase>> userTransactionCache) {
        this.userTransactionCache = userTransactionCache;
    }
    /**
     * Analyzes a user's transaction history to detect patterns and anomalies.
     *
     * @param currentPurchase The current Purchase instance to be analyzed.
     * @return A HistoricalAnalysis object containing statistical measures and patterns detected in the user's transaction history.
     */
    public HistoricalAnalysis analyzeUserHistory(Purchase currentPurchase) {
        List<Purchase> userHistory = userTransactionCache.get(currentPurchase.getUserId());
        if (userHistory == null || userHistory.isEmpty()) {
            return new HistoricalAnalysis();
        }

        // Calculate statistical measures and patterns
        // explaination
        // 1. Calculate the average amount of purchases in the user's transaction history.
        // 2. Calculate the standard deviation of the amounts in the user's transaction history.
        // 3. Identify the common merchants in the user's transaction history.
        // 4. Analyze the typical purchase hours in the user's transaction history.
        // 5. Calculate the frequency score of purchases in the last 24 hours.
        // 6. Detect unusual patterns in the user's transaction history compared to the current purchase.
        HistoricalAnalysis analysis = new HistoricalAnalysis();
        analysis.setAverageAmount(calculateAverageAmount(userHistory));
        analysis.setStandardDeviation(calculateStandardDeviation(userHistory));
        analysis.setCommonMerchants(findCommonMerchants(userHistory));
        analysis.setTypicalPurchaseHours(analyzeTypicalHours(userHistory));
        analysis.setPurchaseFrequencyScore(calculateFrequencyScore(userHistory));
        analysis.setUnusualPatternScore(detectUnusualPatterns(userHistory, currentPurchase));
        
        return analysis;
    }

    /**
     * Calculates the average amount of purchases in the given transaction history.
     *
     * @param history A list of Purchase instances representing the user's transaction history.
     * @return The average amount of the purchases. If the history is empty, returns 0.0.
     */
    private double calculateAverageAmount(List<Purchase> history) {
        return history.stream()
            .mapToDouble(Purchase::getAmount) // Convert each Purchase to its amount
            .average() // Calculate the average of the amounts
            .orElse(0.0); // If the history is empty, return 0.0
    }

    /**
     * Calculates the standard deviation of the amounts in the given transaction history.
     *
     * @param history A list of Purchase instances representing the user's transaction history.
     * @return The standard deviation of the purchase amounts. If the history is empty, returns 0.0.
     */
    private double calculateStandardDeviation(List<Purchase> history) {
        // Calculate the mean (average) amount of the purchases
        double mean = calculateAverageAmount(history);
        
        // Calculate the variance
        double variance = history.stream()
            .mapToDouble(p -> Math.pow(p.getAmount() - mean, 2)) // Calculate the squared difference from the mean for each purchase
            .average() // Calculate the average of these squared differences
            .orElse(0.0); // If the history is empty, return 0.0
        
        // Return the square root of the variance, which is the standard deviation
        return Math.sqrt(variance);
    }

    /**
     * Finds the common merchants in the user's transaction history.
     *
     * @param history A list of Purchase instances representing the user's transaction history.
     * @return A map where the keys are merchant names and the values are the number of times the user has made a purchase from each merchant.
     */
    private Map<String, Integer> findCommonMerchants(List<Purchase> history) {
        return history.stream()
            .collect(Collectors.groupingBy(
                Purchase::getMerchantName, // Group by merchant name
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue) // Count the occurrences and convert to Integer
            ));
    }

    /**
     * Analyzes the typical purchase hours in the user's transaction history.
     *
     * @param history A list of Purchase instances representing the user's transaction history.
     * @return A map where the keys are hours of the day (0-23) and the values are the number of purchases made during each hour.
     */
    private Map<Integer, Integer> analyzeTypicalHours(List<Purchase> history) {
        return history.stream()
            .collect(Collectors.groupingBy(
                p -> p.getTimestamp().getHour(), // Group by the hour of the purchase
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue) // Count the occurrences and convert to Integer
            ));
    }

    /**
     * Calculates the frequency score of purchases in the last 24 hours.
     *
     * @param history A list of Purchase instances representing the user's transaction history.
     * @return The frequency score, which is the number of transactions per hour in the last 24 hours.
     */
    private double calculateFrequencyScore(List<Purchase> history) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayAgo = now.minusDays(1);
        
        long recentTransactions = history.stream()
            .filter(p -> p.getTimestamp().isAfter(dayAgo)) // Filter transactions from the last 24 hours
            .count();
            
        return recentTransactions / 24.0; // Calculate transactions per hour
    }

    /**
     * Detects unusual patterns in the user's transaction history compared to the current purchase.
     *
     * @param history A list of Purchase instances representing the user's transaction history.
     * @param currentPurchase The current Purchase instance to be analyzed.
     * @return A score indicating the likelihood of unusual patterns. Higher scores indicate more unusual patterns.
     */
    private double detectUnusualPatterns(List<Purchase> history, Purchase currentPurchase) {
        double score = 0.0;
        double avgAmount = calculateAverageAmount(history);
        double stdDev = calculateStandardDeviation(history);

        // Amount deviation
        if (Math.abs(currentPurchase.getAmount() - avgAmount) > (2 * stdDev)) {
            score += 0.4;
        }

        // Unusual hour
        Map<Integer, Integer> typicalHours = analyzeTypicalHours(history);
        int currentHour = currentPurchase.getTimestamp().getHour();
        if (!typicalHours.containsKey(currentHour) || typicalHours.get(currentHour) < 2) {
            score += 0.3;
        }

        // Unusual merchant
        Map<String, Integer> merchants = findCommonMerchants(history);
        if (!merchants.containsKey(currentPurchase.getMerchantName())) {
            score += 0.3;
        }

        return score;
    }
}