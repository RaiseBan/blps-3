package com.example.blps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Конфигурация для интеграции с Битрикс24.
 * Содержит настройки для подключения к API Битрикс24.
 */
@Configuration
@PropertySource("classpath:bitrix24.properties")
public class Bitrix24Config {

    @Value("${bitrix.webhook.url}")
    private String webhookUrl;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}