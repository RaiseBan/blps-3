package com.example.blps.service.auth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * MD5 encoder для работы с паролями в формате {MD5}HASH
 */
@Component
public class Md5PasswordEncoder implements PasswordEncoder {

    /**
     * Генерирует MD5 хеш пароля с префиксом {MD5}
     */
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(rawPassword.toString().getBytes());
            String hash = new BigInteger(1, md.digest()).toString(16);
            while (hash.length() < 32) {
                hash = "0" + hash;
            }
            return "{MD5}" + hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка при создании MD5 хеша", e);
        }
    }

    /**
     * Сравнивает пароль и его хеш
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }

        // Создаем хеш для введенного пароля
        String rawHash = encode(rawPassword);

        boolean result = encodedPassword.equalsIgnoreCase(rawHash);
        System.out.println("Сравнение паролей:");
        System.out.println("  Введенный пароль: " + rawPassword);
        System.out.println("  Хеш введенного пароля: " + rawHash);
        System.out.println("  Хеш из хранилища: " + encodedPassword);
        System.out.println("  Результат: " + result);
        return result;
    }
}
