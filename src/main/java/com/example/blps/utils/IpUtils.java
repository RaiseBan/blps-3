package com.example.blps.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Утилита для работы с IP адресами
 */
@Slf4j
public class IpUtils {
    
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };
    
    /**
     * Извлекает IP адрес клиента из запроса
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // Обрабатываем случай с несколькими IP адресами в заголовке
                String[] ips = ip.split(",");
                return ips[0].trim();
            }
        }
        
        String remoteAddr = request.getRemoteAddr();
        log.debug("Client IP resolved to: {}", remoteAddr);
        return remoteAddr;
    }
}