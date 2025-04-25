package com.example.blps.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.ResourceWarning;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.example.blps.connector.record.BitrixInteractionSpec;
import com.example.blps.connector.record.BitrixMappedRecord;

import lombok.extern.slf4j.Slf4j;

/**
 * Реализация взаимодействия для Битрикс24 коннектора.
 * Выполняет HTTP запросы к API Битрикс24.
 */
@Slf4j
public class Bitrix24Interaction implements Interaction {

    private final CloseableHttpClient httpClient;
    private final Connection connection;
    private ResourceWarning warnings;

    public Bitrix24Interaction() {
        this.httpClient = HttpClients.createDefault();
        this.connection = null;
    }

    /**
     * Создает взаимодействие с указанным соединением.
     */
    public Bitrix24Interaction(Connection connection) {
        this.httpClient = HttpClients.createDefault();
        this.connection = connection;
    }

    @Override
    public boolean execute(InteractionSpec ispec, Record input, Record output) throws ResourceException {
        if (!(ispec instanceof BitrixInteractionSpec)) {
            throw new ResourceException("Expected BitrixInteractionSpec");
        }

        if (!(input instanceof BitrixMappedRecord)) {
            throw new ResourceException("Expected BitrixMappedRecord as input");
        }

        if (!(output instanceof BitrixMappedRecord)) {
            throw new ResourceException("Expected BitrixMappedRecord as output");
        }

        BitrixInteractionSpec spec = (BitrixInteractionSpec) ispec;
        BitrixMappedRecord inputRecord = (BitrixMappedRecord) input;
        BitrixMappedRecord outputRecord = (BitrixMappedRecord) output;

        try {
            // Формируем URL для запроса
            String url = spec.getWebhookUrl() + spec.getMethod();
            log.debug("Executing request to URL: {}", url);

            // Создаем HTTP POST запрос
            HttpPost httpPost = new HttpPost(new URI(url));

            // Добавляем параметры из входной записи
            List<NameValuePair> params = convertMapToNameValuePairs(inputRecord.getParameters());
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            // Выполняем запрос
            CloseableHttpResponse response = httpClient.execute(httpPost);

            try {
                // Обрабатываем ответ
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    // Добавляем предупреждение о неуспешном статусе
                    addWarning("HTTP status code: " + statusCode);
                }

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String responseString = EntityUtils.toString(entity);
                    outputRecord.setResponse(responseString);
                    return true;
                } else {
                    log.error("No response entity from Bitrix24");
                    addWarning("No response entity from Bitrix24");
                    return false;
                }
            } finally {
                response.close();
            }
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            log.error("Error preparing request to Bitrix24", e);
            addWarning("Error preparing request: " + e.getMessage());
            throw new ResourceException("Error preparing request to Bitrix24", e);
        } catch (IOException e) {
            log.error("Error executing request to Bitrix24", e);
            addWarning("Error executing request: " + e.getMessage());
            throw new ResourceException("Error executing request to Bitrix24", e);
        }
    }

    @Override
    public Record execute(InteractionSpec ispec, Record input) throws ResourceException {
        throw new ResourceException("This method is not supported, use execute(InteractionSpec, Record, Record) instead");
    }

    @Override
    public void close() throws ResourceException {
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new ResourceException("Error closing HTTP client", e);
        }
    }

    @Override
    public Connection getConnection() {
        if (connection == null) {
            throw new RuntimeException("No connection associated with this interaction");
        }
        return connection;
    }

    @Override
    public ResourceWarning getWarnings() throws ResourceException {
        return warnings;
    }

    @Override
    public void clearWarnings() throws ResourceException {
        warnings = null;
    }

    /**
     * Добавляет предупреждение в цепочку предупреждений.
     *
     * @param message сообщение предупреждения
     */
    private void addWarning(String message) {
        // В реальной реализации здесь был бы код создания и добавления ResourceWarning,
        // но для простоты мы просто логируем предупреждение
        log.warn("Interaction warning: {}", message);
    }

    /**
     * Преобразует Map в список пар ключ-значение для HTTP запроса
     */
    private List<NameValuePair> convertMapToNameValuePairs(Map<String, Object> map) {
        List<NameValuePair> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                result.add(new BasicNameValuePair(entry.getKey(), value.toString()));
            }
        }
        return result;
    }
}