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
import java.time.temporal.ChronoUnit;
import jakarta.annotation.PostConstruct;

@Service
public class FraudDetectionService {
    private RandomForest classifier;
    private Instances dataStructure;
    final TransactionHistoryAnalyzer historyAnalyzer;

    @PostConstruct
    public void init() {
        try {
            // Initialize ML model
            classifier = new RandomForest();

            // Define attributes for the model
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("amount"));
            attributes.add(new Attribute("hour_of_day"));
            attributes.add(new Attribute("transaction_frequency"));

            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("legitimate");
            classValues.add("fraudulent");
            Attribute classAttribute = new Attribute("class", classValues);
            attributes.add(classAttribute);

            // Create dataset with attributes
            dataStructure = new Instances("FraudDetection", attributes, 0);
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);

            // Add some initial training data
            addTrainingData();

            // Train the model
            trainModel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize fraud detection model", e);
        }
    }

    private void addTrainingData() {
        // Add legitimate transactions
        addInstance(100.0, 14, 0.1, false); // Normal daytime purchase
        addInstance(50.0, 12, 0.2, false); // Small daytime purchase
        addInstance(200.0, 15, 0.3, false); // Medium daytime purchase

        // Add fraudulent transactions
        addInstance(999.9, 3, 0.9, true); // Large nighttime purchase with high frequency
        addInstance(888.8, 2, 0.8, true); // Large nighttime purchase
        addInstance(777.7, 1, 0.7, true); // Large nighttime purchase
    }

    private void addInstance(double amount, int hour, double frequency, boolean isFraud) {
        double[] values = new double[dataStructure.numAttributes()];
        values[0] = normalizeAmount(amount);
        values[1] = normalizeHour(LocalTime.of(hour, 0));
        values[2] = frequency;
        values[3] = isFraud ? 1.0 : 0.0;

        dataStructure.add(new DenseInstance(1.0, values));
    }

    // Add new fields for analysis
    private static final double SUSPICIOUS_AMOUNT_THRESHOLD = 5000.0;
    private static final int SUSPICIOUS_HOUR_START = 23;
    private static final int SUSPICIOUS_HOUR_END = 5;
    private final IgniteCache<Long, List<Purchase>> userTransactionCache;

    public FraudDetectionService(Ignite ignite, TransactionHistoryAnalyzer historyAnalyzer) {
        this.userTransactionCache = ignite.getOrCreateCache("userTransactionCache");
        this.historyAnalyzer = historyAnalyzer;
    }

    public boolean analyzeTransaction(Purchase purchase) {
        try {
            // Get historical analysis
            HistoricalAnalysis history = historyAnalyzer.analyzeUserHistory(purchase);

            // Add historical features to ML model
            double[] values = new double[dataStructure.numAttributes()];
            values[0] = normalizeAmount(purchase.getAmount());
            values[1] = normalizeHour(LocalTime.from(purchase.getTimestamp()));
            values[2] = history.getPurchaseFrequencyScore();
            values[3] = history.getUnusualPatternScore();

            DenseInstance instance = new DenseInstance(1.0, values);
            instance.setDataset(dataStructure);

            double mlPrediction = classifier.classifyInstance(instance);

            // Enhanced fraud detection with historical data
            boolean isSuspicious = isTransactionSuspicious(purchase, history);
            boolean hasUnusualPattern = history.getUnusualPatternScore() > 0.7;
            boolean isLocationSuspicious = isLocationSuspicious(purchase);

            return mlPrediction == 1.0 ||
                    (isSuspicious && (hasUnusualPattern || isLocationSuspicious));

        } catch (Exception e) {
            return handleAnalysisError(purchase, e);
        }
    }

    private boolean isTransactionSuspicious(Purchase purchase, HistoricalAnalysis history) {
        if (purchase.getAmount() > history.getAverageAmount() + (3 * history.getStandardDeviation())) {
            return true;
        }

        int hour = purchase.getTimestamp().getHour();
        Map<Integer, Integer> typicalHours = history.getTypicalPurchaseHours();
        if (!typicalHours.containsKey(hour) || typicalHours.get(hour) < 2) {
            return true;
        }

        return false;
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