package com.ntn.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:oauth2.properties")
public class OAuth2Config {
    // Load file cấu hình oauth2.properties
}