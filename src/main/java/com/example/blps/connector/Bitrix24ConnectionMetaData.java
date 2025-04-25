package com.example.blps.connector;

import jakarta.resource.cci.ConnectionMetaData;

/**
 * Метаданные соединения для Битрикс24 коннектора.
 */
public class Bitrix24ConnectionMetaData implements ConnectionMetaData {

    @Override
    public String getEISProductName() {
        return "Bitrix24";
    }

    @Override
    public String getEISProductVersion() {
        return "REST API";
    }

    @Override
    public String getUserName() {
        return "Webhook";
    }
}