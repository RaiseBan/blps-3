package com.example.blps.connector.record;

import jakarta.resource.cci.InteractionSpec;

import lombok.Getter;

@Getter
public class BitrixInteractionSpec implements InteractionSpec {

    private static final long serialVersionUID = 1L;

    private final String method;

    private final String webhookUrl;

    public BitrixInteractionSpec(String method, String webhookUrl) {
        this.method = method;
        this.webhookUrl = webhookUrl;
    }
}