package com.example.blps.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.transport.stomp.StompConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StompMessageService {

    private StompConnection connection;
    private final ObjectMapper objectMapper;
    private volatile boolean connected = false;

    @Value("${spring.activemq.stomp.host:localhost}")
    private String stompHost;

    @Value("${spring.activemq.stomp.port:61613}")
    private int stompPort;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    public StompMessageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            connect();
        } catch (Exception e) {
            log.error("Failed to initialize STOMP connection", e);
        }
    }

    private void connect() {
        try {
            connection = new StompConnection();
            connection.open(stompHost, stompPort);
            connection.connect(username, password);
            connected = true;

            log.info("Successfully connected to ActiveMQ via STOMP at {}:{}", stompHost, stompPort);
        } catch (Exception e) {
            connected = false;
            log.error("Failed to connect to ActiveMQ via STOMP", e);
        }
    }

    /**
     * Отправляет сообщение через STOMP для получения через JMS
     */
    public void sendMessage(String destination, Object message) {
        sendMessage(destination, message, null);
    }

    /**
     * Отправляет сообщение через STOMP с дополнительными headers
     */
    public void sendMessage(String destination, Object message, Map<String, String> additionalHeaders) {
        try {
            if (!connected) {
                log.warn("STOMP connection not established, attempting reconnect");
                connect();
            }

            if (connected && connection != null) {
                // Сериализуем объект в JSON
                String jsonMessage = objectMapper.writeValueAsString(message);

                // Создаем headers для правильной десериализации в JMS
                HashMap<String, String> headers = new HashMap<>();
                headers.put("persistent", "true");
                headers.put("content-type", "application/json");
                headers.put("_type", message.getClass().getName());

                // Добавляем дополнительные headers если есть
                if (additionalHeaders != null) {
                    headers.putAll(additionalHeaders);
                }

                // Отправляем в очередь (без префикса /queue/)
                connection.send(destination, jsonMessage, null, headers);

                log.debug("Message sent via STOMP to destination: {}", destination);
            } else {
                log.warn("Unable to send message - STOMP connection not available");
            }
        } catch (Exception e) {
            connected = false;
            log.error("Error sending message via STOMP: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (connection != null) {
                connection.disconnect();
                connected = false;
                log.info("STOMP connection closed");
            }
        } catch (Exception e) {
            log.error("Error closing STOMP connection", e);
        }
    }
}