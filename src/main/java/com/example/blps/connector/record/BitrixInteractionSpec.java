package com.example.blps.connector.record;

import jakarta.resource.cci.InteractionSpec;

import lombok.Getter;

/**
 * Спецификация взаимодействия для Битрикс24 коннектора.
 * Содержит информацию о методе API и URL вебхука.
 */
@Getter
public class BitrixInteractionSpec implements InteractionSpec {

    private static final long serialVersionUID = 1L;

    // Метод API Битрикс24
    private final String method;

    // URL вебхука для доступа к API
    private final String webhookUrl;

    public BitrixInteractionSpec(String method, String webhookUrl) {
        this.method = method;
        this.webhookUrl = webhookUrl;
    }
}