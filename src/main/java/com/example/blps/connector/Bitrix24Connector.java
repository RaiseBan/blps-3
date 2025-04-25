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

/**
 * JCA коннектор для интеграции с Битрикс24 CRM.
 * Реализует отправку запросов через REST API Битрикс24.
 */
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

    /**
     * Отправляет запрос к API Битрикс24 и возвращает ответ.
     *
     * @param method имя метода API
     * @param params параметры запроса
     * @return результат запроса
     * @throws ResourceException если произошла ошибка при взаимодействии с API
     */
    public String executeMethod(String method, java.util.Map<String, Object> params) throws ResourceException {
        log.debug("Executing Bitrix24 method: {} with params: {}", method, params);

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            Interaction interaction = connection.createInteraction();

            // Создаем спецификацию взаимодействия
            InteractionSpec interactionSpec = new BitrixInteractionSpec(method, webhookUrl);

            // Создаем и заполняем входную запись
            BitrixMappedRecord inputRecord = (BitrixMappedRecord) recordFactory.createMappedRecord("BitrixRequest");
            inputRecord.setRecordName("BitrixRequest");
            inputRecord.setParameters(params);

            // Создаем выходную запись
            Record outputRecord = recordFactory.createMappedRecord("BitrixResponse");

            // Выполняем запрос
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

    /**
     * Создает задачу в Битрикс24.
     *
     * @param title название задачи
     * @param description описание задачи
     * @param responsibleId ID ответственного сотрудника
     * @return ID созданной задачи
     * @throws ResourceException если произошла ошибка при создании задачи
     */
    public String createTask(String title, String description, String responsibleId) throws ResourceException {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("fields[TITLE]", title);
        params.put("fields[DESCRIPTION]", description);
        params.put("fields[RESPONSIBLE_ID]", responsibleId);

        return executeMethod("tasks.task.add", params);
    }

    /**
     * Создает лид в Битрикс24 CRM.
     *
     * @param title название лида
     * @param name имя контакта
     * @param email email контакта
     * @param phone телефон контакта
     * @return ID созданного лида
     * @throws ResourceException если произошла ошибка при создании лида
     */
    public String createLead(String title, String name, String email, String phone) throws ResourceException {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("fields[TITLE]", title);
        params.put("fields[NAME]", name);
        params.put("fields[EMAIL][0][VALUE]", email);
        params.put("fields[EMAIL][0][VALUE_TYPE]", "WORK");
        params.put("fields[PHONE][0][VALUE]", phone);
        params.put("fields[PHONE][0][VALUE_TYPE]", "WORK");

        return executeMethod("crm.lead.add", params);
    }
}