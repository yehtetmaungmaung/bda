package com.example.backend.service;

import com.example.backend.model.Purchase;
import com.example.backend.service.fraud.FraudDetectionService;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PurchaseService {
    private final IgniteCache<Long, Purchase> purchaseCache;
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final FraudDetectionService fraudDetectionService;

    public PurchaseService(Ignite ignite, FraudDetectionService fraudDetectionService) {
        this.purchaseCache = ignite.getOrCreateCache("purchaseCache");
        this.fraudDetectionService = fraudDetectionService;
    }

    public Purchase processPurchase(Purchase purchase) {
        purchase.setId(idGenerator.incrementAndGet());
        purchase.setTimestamp(LocalDateTime.now());
        
        // Perform real-time fraud detection
        boolean isFraudulent = fraudDetectionService.analyzeTransaction(purchase);
        purchase.setFraud(isFraudulent);
        
        purchaseCache.put(purchase.getId(), purchase);
        return purchase;
    }

    public Purchase getPurchase(Long id) {
        return purchaseCache.get(id);
    }
}