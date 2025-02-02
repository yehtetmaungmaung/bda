package com.example.backend.model;

import java.util.HashSet;
import java.util.Set;

public class User {
    private Long id;
    private String name;
    private String email;
    private Set<String> cardNumbers = new HashSet<>();
    private double riskScore;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getCardNumbers() {
        return cardNumbers;
    }

    public void setCardNumbers(Set<String> cardNumbers) {
        this.cardNumbers = cardNumbers;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }
}