package com.example.backend.config;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class IgniteConfig {

    @Bean
    public Ignite igniteInstance() throws IgniteException {
        IgniteConfiguration cfg = new IgniteConfiguration();
        
        // Configure node as client
        cfg.setClientMode(true);
        
        // Disable peer class loading
        cfg.setPeerClassLoadingEnabled(true);
        
        // Configure TCP discovery
        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        discoverySpi.setIpFinder(ipFinder);
        cfg.setDiscoverySpi(discoverySpi);
        
        // Set instance name
        cfg.setIgniteInstanceName("bda-project-instance");
        
        // Set system-wide local address or interface
        cfg.setLocalHost("127.0.0.1");
        
        return Ignition.start(cfg);
    }
}