package com.example.blps.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> userInfo = new HashMap<>();

        if (authentication == null || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            userInfo.put("status", "not_authenticated");
            userInfo.put("message", "Пользователь не аутентифицирован");
            return ResponseEntity.ok(userInfo);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        userInfo.put("status", "authenticated");
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("authorities", userDetails.getAuthorities().toString());

        return ResponseEntity.ok(userInfo);
    }
}