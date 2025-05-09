package com.example.blps.service.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jakarta.annotation.PostConstruct;

@Service
public class XmlUserDetailsService implements UserDetailsService {

    @Value("classpath:users.xml")
    private Resource xmlResource;

    private final Map<String, UserDetails> userCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            loadUsers();
            System.out.println("Пользователи загружены: " + userCache.size());
            userCache.forEach((name, details) -> {
                System.out.println("Пользователь: " + name
                        + ", пароль: " + details.getPassword()
                        + ", роли: " + details.getAuthorities());
            });
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке пользователей: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось загрузить пользователей", e);
        }
    }

    private void loadUsers() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlResource.getInputStream());

        NodeList userElements = document.getElementsByTagName("user");
        for (int i = 0; i < userElements.getLength(); i++) {
            Element userElement = (Element) userElements.item(i);

            String username = userElement.getAttribute("username");
            String password = userElement.getAttribute("password");

            Collection<GrantedAuthority> authorities = new ArrayList<>();

            NodeList roleElements = userElement.getElementsByTagName("role");
            for (int j = 0; j < roleElements.getLength(); j++) {
                Element roleElement = (Element) roleElements.item(j);
                String roleName = roleElement.getTextContent().trim();
                if (!roleName.startsWith("ROLE_")) {
                    roleName = "ROLE_" + roleName;
                }
                authorities.add(new SimpleGrantedAuthority(roleName));
            }

            UserDetails user = ImmutableUserDetails.builder()
                    .username(username)
                    .password(password) 
                    .authorities(authorities)
                    .build();

            userCache.put(username, user);
            System.out.println("Загружен пользователь: " + username + ", пароль: " + password);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Запрошен пользователь: " + username);

        UserDetails user = userCache.get(username);
        if (user == null) {
            System.err.println("Пользователь не найден: " + username);
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        UserDetails userCopy = ImmutableUserDetails.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getAuthorities())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .enabled(user.isEnabled())
                .build();

        System.out.println("Найден пользователь: " + username
                + ", пароль: " + userCopy.getPassword()
                + ", роли: " + userCopy.getAuthorities());
        return userCopy;
    }
}