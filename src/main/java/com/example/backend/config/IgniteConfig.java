package com.example.backend.config;

import com.example.backend.model.Purchase;
import com.example.backend.model.User;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class IgniteConfig {

    @Bean
    public Ignite igniteInstance() {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        spi.setIpFinder(ipFinder);

        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName("bda-project-instance");
        cfg.setClientMode(true);
        cfg.setDiscoverySpi(spi);
        cfg.setPeerClassLoadingEnabled(true);

        return Ignition.start(cfg);
    }

    @Bean
    public IgniteCache<Long, User> userCache(Ignite ignite) {
        CacheConfiguration<Long, User> cacheCfg = new CacheConfiguration<>("userCache");
        return ignite.getOrCreateCache(cacheCfg);
    }

    @Bean
    public IgniteCache<Long, List<Purchase>> userTransactionCache(Ignite ignite) {
        CacheConfiguration<Long, List<Purchase>> cacheCfg = new CacheConfiguration<>("userTransactionCache");
        return ignite.getOrCreateCache(cacheCfg);
    }

    @Bean
    public IgniteCache<String, Long> cardNumberToUserIdCache(Ignite ignite) {
        CacheConfiguration<String, Long> cacheCfg = new CacheConfiguration<>("cardNumberToUserIdCache");
        return ignite.getOrCreateCache(cacheCfg);
    }
}