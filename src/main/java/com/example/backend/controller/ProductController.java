package com.example.backend.controller;

import com.example.backend.model.Product;
import com.example.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;

    @GetMapping("/random")
    public List<Product> getRandomProducts(@RequestParam(defaultValue = "9") int count) {
        return productService.getRandomProducts(count);
    }
}