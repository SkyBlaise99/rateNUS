package com.rateNUS.backend.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides initial sample data to database used by repositories.
 */
@Configuration
public class Config {
    public static final String frontendURL = "http://ratenus.com/";

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {};
    }
}
