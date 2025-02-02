package com.example.backend.service;

import com.example.backend.model.User;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final IgniteCache<Long, User> userCache;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public UserService(Ignite ignite) {
        this.userCache = ignite.getOrCreateCache("userCache");
    }

    public User saveUser(User user) {
        user.setId(idGenerator.incrementAndGet());
        userCache.put(user.getId(), user);
        return user;
    }

    public User getUser(Long id) {
        return userCache.get(id);
    }
}