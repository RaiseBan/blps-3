package com.example.blps.service.notification;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    public StompMessageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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

    public void sendMessage(String destination, Object message) {
        sendMessage(destination, message, null);
    }

    public void sendMessage(String destination, Object message, Map<String, String> additionalHeaders) {
        try {
            if (!connected) {
                log.warn("STOMP connection not established, attempting reconnect");
                connect();
            }

            if (connected && connection != null) {
                
                String jsonMessage = objectMapper.writeValueAsString(message);

                HashMap<String, String> headers = new HashMap<>();
                headers.put("persistent", "true");
                headers.put("content-type", "application/json");
                headers.put("_type", message.getClass().getName());

                if (additionalHeaders != null) {
                    headers.putAll(additionalHeaders);
                }

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