package com.example.backend.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private Double price;
    private String category;
    private String description;
    private String imageUrl;
}