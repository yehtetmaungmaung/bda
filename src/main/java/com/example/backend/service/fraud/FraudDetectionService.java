package com.example.backend.service.fraud;

import com.example.backend.model.Purchase;
import com.example.backend.model.HistoricalAnalysis;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;
import jakarta.annotation.PostConstruct;

/**
 * The FraudDetectionService class is responsible for analyzing transactions to detect potential fraud.
 * It uses a combination of machine learning and historical analysis to determine if a transaction is suspicious.
 */
@Service
public class FraudDetectionService {
    private RandomForest classifier;
    private Instances dataStructure;
    final TransactionHistoryAnalyzer historyAnalyzer;

    @PostConstruct
    public void init() {
        try {
            classifier = new RandomForest();

            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("user_id"));  // Add user_id attribute
            attributes.add(new Attribute("amount"));
            attributes.add(new Attribute("hour_of_day"));
            attributes.add(new Attribute("transaction_frequency"));

            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("legitimate");
            classValues.add("fraudulent");
            attributes.add(new Attribute("class", classValues));

            dataStructure = new Instances("FraudDetection", attributes, 0);
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);

            addTrainingData();
            trainModel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize fraud detection model", e);
        }
    }

    private void addTrainingData() {
        // User 1 - Regular daytime shopper
        addInstance(1L, 100.0, 14, 0.1, false);
        addInstance(1L, 500.0, 13, 0.2, false);
        addInstance(1L, 1200.0, 15, 0.1, false);

        // User 2 - Night shift worker (legitimate night purchases)
        addInstance(2L, 800.0, 23, 0.3, false);
        addInstance(2L, 150.0, 2, 0.1, false);
        addInstance(2L, 1500.0, 1, 0.2, false);

        // User 3 - High value purchaser
        addInstance(3L, 2000.0, 13, 0.2, false);
        addInstance(3L, 2500.0, 14, 0.3, false);

        // Fraudulent patterns (different users)
        addInstance(1L, 2000.0, 2, 0.9, true);   // Unusual for User 1
        addInstance(2L, 1500.0, 14, 0.8, true);  // Unusual time for User 2
        addInstance(3L, 100.0, 3, 0.95, true);   // Unusual amount for User 3
    }

    private void addInstance(Long userId, double amount, int hour, double frequency, boolean isFraud) {
        double[] values = new double[dataStructure.numAttributes()];
        values[0] = normalizeUserId(userId);
        values[1] = normalizeAmount(amount);
        values[2] = normalizeHour(LocalTime.of(hour, 0));
        values[3] = frequency;
        values[4] = isFraud ? 1.0 : 0.0;

        dataStructure.add(new DenseInstance(1.0, values));
    }

    private double normalizeUserId(Long userId) {
        // Simple normalization for demo purposes
        return userId / 1000.0;
    }

    // Add new fields for analysis
    private final IgniteCache<Long, List<Purchase>> userTransactionCache;

    /**
     * Initializes the FraudDetectionService with the given Ignite instance and TransactionHistoryAnalyzer.
     *
     * @param ignite          An instance of Ignite used to initialize the caches.
     * @param historyAnalyzer An instance of TransactionHistoryAnalyzer used for historical analysis.
     */
    public FraudDetectionService(Ignite ignite, TransactionHistoryAnalyzer historyAnalyzer) {
        this.userTransactionCache = ignite.getOrCreateCache("userTransactionCache");
        this.historyAnalyzer = historyAnalyzer;
    }

    /**
     * Analyzes the given transaction to determine if it is fraudulent.
     *
     * @param purchase An instance of Purchase representing the transaction to be analyzed.
     * @return A boolean indicating whether the transaction is fraudulent.
     */
    // Update thresholds
    private static final double SUSPICIOUS_AMOUNT_THRESHOLD = 2000.0; // Lower to more realistic value
    private static final double HIGH_FREQUENCY_THRESHOLD = 0.6;
    private static final int SUSPICIOUS_TIME_WINDOW = 24; // hours

    public boolean analyzeTransaction(Purchase purchase) {
        try {
            HistoricalAnalysis history = historyAnalyzer.analyzeUserHistory(purchase);
            boolean isFirstPurchase = isFirstPurchaseForUser(purchase.getUserId());

            // ML model prediction
            double[] values = new double[dataStructure.numAttributes()];
            values[0] = normalizeUserId(purchase.getUserId());
            values[1] = normalizeAmount(purchase.getAmount());
            values[2] = normalizeHour(LocalTime.from(purchase.getTimestamp()));
            values[3] = history.getPurchaseFrequencyScore();

            DenseInstance instance = new DenseInstance(1.0, values);
            instance.setDataset(dataStructure);
            double mlPrediction = classifier.classifyInstance(instance);

            // Enhanced fraud detection logic
            if (isFirstPurchase) {
                // For first purchase, check only basic risk factors
                return purchase.getAmount() > SUSPICIOUS_AMOUNT_THRESHOLD || 
                       isUnusualPurchaseTime(purchase.getTimestamp().getHour());
            }

            // Combine multiple risk factors
            int riskFactors = 0;
            if (mlPrediction == 1.0) riskFactors++;
            if (isTransactionSuspicious(purchase, history)) riskFactors++;
            if (history.getPurchaseFrequencyScore() > HIGH_FREQUENCY_THRESHOLD) riskFactors++;
            if (isLocationSuspicious(purchase)) riskFactors++;
            if (history.getUnusualPatternScore() > 0.7) riskFactors++;

            // Mark as fraud if multiple risk factors are present
            return riskFactors >= 2;

        } catch (Exception e) {
            return handleAnalysisError(purchase, e);
        }
    }

    private boolean isUnusualPurchaseTime(int hour) {
        // Consider early morning hours (2 AM - 5 AM) as unusual
        return hour >= 2 && hour <= 5;
    }

    private boolean isTransactionSuspicious(Purchase purchase, HistoricalAnalysis history) {
        List<Purchase> userHistory = getUserTransactionHistory(purchase.getUserId());
        
        if (userHistory == null || userHistory.isEmpty()) {
            return purchase.getAmount() > SUSPICIOUS_AMOUNT_THRESHOLD;
        }

        // Check multiple suspicious factors
        int suspiciousFactors = 0;

        // Amount check
        double userAvgAmount = history.getAverageAmount();
        double userStdDev = history.getStandardDeviation();
        if (purchase.getAmount() > userAvgAmount + (2 * userStdDev)) {
            suspiciousFactors++;
        }

        // Time pattern check
        int hour = purchase.getTimestamp().getHour();
        Map<Integer, Integer> typicalHours = history.getTypicalPurchaseHours();
        if (!typicalHours.containsKey(hour) || typicalHours.get(hour) < 2) {
            suspiciousFactors++;
        }

        // Frequency check
        if (history.getPurchaseFrequencyScore() > HIGH_FREQUENCY_THRESHOLD) {
            suspiciousFactors++;
        }

        return suspiciousFactors >= 2;
    }

    private boolean isFirstPurchaseForUser(Long userId) {
        List<Purchase> history = getUserTransactionHistory(userId);
        return history == null || history.isEmpty();
    }


    private boolean isLocationSuspicious(Purchase purchase) {
        List<Purchase> userHistory = getUserTransactionHistory(purchase.getUserId());
        if (userHistory == null || userHistory.isEmpty()) {
            return false;
        }

        // Get user's last transaction location
        Purchase lastTransaction = userHistory.get(userHistory.size() - 1);

        // Check if location changed too quickly (impossible travel)
        if (lastTransaction != null &&
                lastTransaction.getTimestamp().plusHours(2).isAfter(purchase.getTimestamp())) {
            // Simple distance check (should be replaced with actual geo calculation)
            return !lastTransaction.getMerchantName().equals(purchase.getMerchantName());
        }

        return false;
    }

    private List<Purchase> getUserTransactionHistory(Long userId) {
        return userTransactionCache.get(userId);
    }

    private boolean handleAnalysisError(Purchase purchase, Exception e) {
        // Log the error
        System.err.println("Error analyzing transaction: " + e.getMessage());
        // In case of error, flag high-value transactions as suspicious
        return purchase.getAmount() > SUSPICIOUS_AMOUNT_THRESHOLD;
    }

    private double normalizeAmount(double amount) {
        // Simple min-max normalization (assuming max transaction is 10000)
        return Math.min(amount / 10000.0, 1.0);
    }

    private double normalizeHour(LocalTime time) {
        // Convert hour to value between 0 and 1
        return time.getHour() / 24.0;
    }

    private void trainModel() {
        try {
            // In production, this would load a pre-trained model
            classifier.buildClassifier(dataStructure);
        } catch (Exception e) {
            throw new RuntimeException("Failed to train fraud detection model", e);
        }
    }
}