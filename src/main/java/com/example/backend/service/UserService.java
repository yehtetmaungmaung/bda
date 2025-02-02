package com.example.backend.service;

import com.example.backend.model.User;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final IgniteCache<Long, User> userCache;
    private final IgniteCache<String, Long> cardNumberToUserIdCache;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public UserService(Ignite ignite) {
        this.userCache = ignite.getOrCreateCache("userCache");
        this.cardNumberToUserIdCache = ignite.getOrCreateCache("cardNumberToUserIdCache");
    }

    public User saveUser(User user) {
        user.setId(idGenerator.incrementAndGet());
        userCache.put(user.getId(), user);
        
        // Map card numbers to user ID for quick lookup
        user.getCardNumbers().forEach(cardNumber -> 
            cardNumberToUserIdCache.put(cardNumber, user.getId())
        );
        
        return user;
    }

    public User getUser(Long id) {
        return userCache.get(id);
    }

    public User getUserByCardNumber(String cardNumber) {
        Long userId = cardNumberToUserIdCache.get(cardNumber);
        return userId != null ? getUser(userId) : null;
    }

    public void updateUserRiskScore(Long userId, double newRiskScore) {
        User user = getUser(userId);
        if (user != null) {
            user.setRiskScore(newRiskScore);
            userCache.put(userId, user);
        }
    }
}