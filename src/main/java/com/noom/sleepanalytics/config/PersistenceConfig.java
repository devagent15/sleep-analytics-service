package com.noom.sleepanalytics.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
