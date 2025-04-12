package com.example.blps.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        System.out.println("DaoAuthenticationProvider создан с passwordEncoder: " + passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authManager() {
        return new ProviderManager(authProvider());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("Настройка SecurityFilterChain");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(authManager())
                .authorizeHttpRequests(auth -> auth
                // Публичные эндпоинты
                .requestMatchers("/").permitAll()
                .requestMatchers("/redirect/**").permitAll()
                .requestMatchers("/api/auth/info").permitAll()
                // Сначала более специфичные правила, которые требуют ADMIN
//                .requestMatchers("/api/our-campaigns/*/optimize-budget").hasRole("ADMIN") // Только супер-админ может оптимизировать бюджет
                .requestMatchers(HttpMethod.DELETE, "/api/our-campaigns/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/their-campaigns/**").hasRole("ADMIN")
                // Затем правила для CAMPAIGN_MANAGER и ADMIN
                .requestMatchers(HttpMethod.POST, "/api/our-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/our-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/their-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/their-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers("/api/their-campaigns/import").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                // Затем правила для просмотра (ADMIN, CAMPAIGN_MANAGER, ANALYST)
                .requestMatchers(HttpMethod.GET, "/api/our-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER", "ANALYST")
                .requestMatchers(HttpMethod.GET, "/api/their-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER", "ANALYST")
                .requestMatchers("/api/reports/campaigns/**").hasAnyRole("ADMIN", "ANALYST")
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {
                    System.out.println("Настройка Basic Auth");
                });

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
