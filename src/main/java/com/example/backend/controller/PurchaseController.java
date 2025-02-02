package com.example.backend.controller;

import com.example.backend.model.Purchase;
import com.example.backend.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    
    private final PurchaseService purchaseService;

    @PostMapping
    public Purchase processPurchase(@RequestBody Purchase purchase) {
        return purchaseService.processPurchase(purchase);
    }

    @GetMapping("/{id}")
    public Purchase getPurchase(@PathVariable Long id) {
        return purchaseService.getPurchase(id);
    }
}