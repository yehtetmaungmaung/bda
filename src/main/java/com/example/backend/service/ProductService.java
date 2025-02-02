package com.example.backend.service;

import com.example.backend.model.Product;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

@Service
public class ProductService {
    private final List<Product> dummyProducts = new ArrayList<>();
    private final Random random = new Random();

    public ProductService() {
        // First 10 products
        dummyProducts.add(new Product(1L, "Smartphone", 999.99, "Electronics", "Latest model smartphone", "/images/products/1.jpg"));
        dummyProducts.add(new Product(2L, "Laptop", 1499.99, "Electronics", "High-performance laptop", "/images/products/2.jpg"));
        dummyProducts.add(new Product(3L, "Headphones", 199.99, "Electronics", "Wireless noise-canceling headphones", "/images/products/3.jpg"));
        dummyProducts.add(new Product(4L, "Smart Watch", 299.99, "Electronics", "Fitness tracking smartwatch", "/images/products/4.jpg"));
        dummyProducts.add(new Product(5L, "Camera", 799.99, "Electronics", "Digital mirrorless camera", "/images/products/5.jpg"));
        dummyProducts.add(new Product(6L, "Gaming Console", 499.99, "Electronics", "Next-gen gaming console", "/images/products/6.jpg"));
        dummyProducts.add(new Product(7L, "Tablet", 699.99, "Electronics", "10-inch tablet", "/images/products/7.jpg"));
        dummyProducts.add(new Product(8L, "Speaker", 159.99, "Electronics", "Bluetooth portable speaker", "/images/products/8.jpg"));
        dummyProducts.add(new Product(9L, "Monitor", 399.99, "Electronics", "27-inch 4K monitor", "/images/products/9.jpg"));
        dummyProducts.add(new Product(10L, "Keyboard", 129.99, "Electronics", "Mechanical gaming keyboard", "/images/products/10.jpg"));

        // 20 additional products
        dummyProducts.add(new Product(11L, "Coffee Maker", 79.99, "Home Appliances", "Programmable coffee maker", "/images/products/11.jpg"));
        dummyProducts.add(new Product(12L, "Air Fryer", 129.99, "Home Appliances", "Digital air fryer with multiple settings", "/images/products/12.jpg"));
        dummyProducts.add(new Product(13L, "Running Shoes", 89.99, "Sports", "Lightweight running shoes", "/images/products/13.jpg"));
        dummyProducts.add(new Product(14L, "Yoga Mat", 29.99, "Sports", "Non-slip exercise yoga mat", "/images/products/14.jpg"));
        dummyProducts.add(new Product(15L, "Backpack", 59.99, "Fashion", "Water-resistant laptop backpack", "/images/products/15.jpg"));
        dummyProducts.add(new Product(16L, "Sunglasses", 149.99, "Fashion", "Polarized designer sunglasses", "/images/products/16.jpg"));
        dummyProducts.add(new Product(17L, "Blender", 69.99, "Home Appliances", "High-speed smoothie blender", "/images/products/17.jpg"));
        dummyProducts.add(new Product(18L, "Smart Bulb Set", 49.99, "Smart Home", "Color-changing smart LED bulbs", "/images/products/18.jpg"));
        dummyProducts.add(new Product(19L, "Wireless Mouse", 39.99, "Electronics", "Ergonomic wireless mouse", "/images/products/19.jpg"));
        dummyProducts.add(new Product(20L, "Desk Chair", 199.99, "Furniture", "Ergonomic office chair", "/images/products/20.jpg"));
        dummyProducts.add(new Product(21L, "Fitness Tracker", 79.99, "Electronics", "Water-resistant fitness band", "/images/products/21.jpg"));
        dummyProducts.add(new Product(22L, "Toaster Oven", 89.99, "Home Appliances", "Digital convection toaster oven", "/images/products/22.jpg"));
        dummyProducts.add(new Product(23L, "Dumbbell Set", 119.99, "Sports", "Adjustable dumbbell set", "/images/products/23.jpg"));
        dummyProducts.add(new Product(24L, "Security Camera", 129.99, "Smart Home", "WiFi security camera with night vision", "/images/products/24.jpg"));
        dummyProducts.add(new Product(25L, "Electric Kettle", 39.99, "Home Appliances", "Temperature control kettle", "/images/products/25.jpg"));
        dummyProducts.add(new Product(26L, "Gaming Mouse", 89.99, "Electronics", "RGB gaming mouse with programmable buttons", "/images/products/26.jpg"));
        dummyProducts.add(new Product(27L, "Portable Charger", 49.99, "Electronics", "20000mAh power bank", "/images/products/27.jpg"));
    }

    public List<Product> getRandomProducts(int count) {
        count = Math.min(count, dummyProducts.size());
        List<Product> randomProducts = new ArrayList<>();
        List<Product> availableProducts = new ArrayList<>(dummyProducts);
        
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(availableProducts.size());
            randomProducts.add(availableProducts.remove(index));
        }
        
        return randomProducts;
    }
}