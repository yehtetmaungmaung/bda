package com.example.backend.model;

import java.util.Map;

public class HistoricalAnalysis {
    private double averageAmount;
    private double standardDeviation;
    private Map<String, Integer> commonMerchants;
    private Map<Integer, Integer> typicalPurchaseHours;
    private double purchaseFrequencyScore;
    private double unusualPatternScore;

    // Default constructor
    public HistoricalAnalysis() {
        this.averageAmount = 0.0;
        this.standardDeviation = 0.0;
        this.purchaseFrequencyScore = 0.0;
        this.unusualPatternScore = 0.0;
    }

    // Getters and Setters
    public double getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(double averageAmount) {
        this.averageAmount = averageAmount;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Map<String, Integer> getCommonMerchants() {
        return commonMerchants;
    }

    public void setCommonMerchants(Map<String, Integer> commonMerchants) {
        this.commonMerchants = commonMerchants;
    }

    public Map<Integer, Integer> getTypicalPurchaseHours() {
        return typicalPurchaseHours;
    }

    public void setTypicalPurchaseHours(Map<Integer, Integer> typicalPurchaseHours) {
        this.typicalPurchaseHours = typicalPurchaseHours;
    }

    public double getPurchaseFrequencyScore() {
        return purchaseFrequencyScore;
    }

    public void setPurchaseFrequencyScore(double purchaseFrequencyScore) {
        this.purchaseFrequencyScore = purchaseFrequencyScore;
    }

    public double getUnusualPatternScore() {
        return unusualPatternScore;
    }

    public void setUnusualPatternScore(double unusualPatternScore) {
        this.unusualPatternScore = unusualPatternScore;
    }
}