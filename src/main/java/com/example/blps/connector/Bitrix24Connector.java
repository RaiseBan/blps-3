package com.example.blps.connector;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.RecordFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.blps.connector.record.BitrixInteractionSpec;
import com.example.blps.connector.record.BitrixMappedRecord;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Bitrix24Connector {

    @Value("${bitrix.webhook.url}")
    private String webhookUrl;

    private final ConnectionFactory connectionFactory;
    private final RecordFactory recordFactory;

    public Bitrix24Connector(ConnectionFactory connectionFactory, RecordFactory recordFactory) {
        this.connectionFactory = connectionFactory;
        this.recordFactory = recordFactory;
    }

    public String executeMethod(String method, java.util.Map<String, Object> params) throws ResourceException {
        log.debug("Executing Bitrix24 method: {} with params: {}", method, params);

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            Interaction interaction = connection.createInteraction();

            InteractionSpec interactionSpec = new BitrixInteractionSpec(method, webhookUrl);

            BitrixMappedRecord inputRecord = (BitrixMappedRecord) recordFactory.createMappedRecord("BitrixRequest");
            inputRecord.setRecordName("BitrixRequest");
            inputRecord.setParameters(params);

            Record outputRecord = recordFactory.createMappedRecord("BitrixResponse");

            boolean success = interaction.execute(interactionSpec, inputRecord, outputRecord);

            if (!success) {
                throw new ResourceException("Failed to execute Bitrix24 method: " + method);
            }

            String result = ((BitrixMappedRecord) outputRecord).getResponse();
            log.debug("Bitrix24 response: {}", result);

            return result;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (ResourceException e) {
                    log.error("Error closing connection", e);
                }
            }
        }
    }
}