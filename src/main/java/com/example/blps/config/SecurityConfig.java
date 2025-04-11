package com.example.blps.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                // Публичные эндпоинты
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/redirect/**").permitAll()
                .requestMatchers("/").permitAll()
                // ADMIN (Супер-админ) - полный доступ ко всем функциям
                .requestMatchers("/api/our-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER", "ANALYST")
                .requestMatchers(HttpMethod.DELETE, "/api/our-campaigns/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/our-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/our-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers("/api/our-campaigns/*/optimize-budget").hasRole("ADMIN") // Только супер-админ может оптимизировать бюджет

                .requestMatchers("/api/their-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER", "ANALYST")
                .requestMatchers(HttpMethod.DELETE, "/api/their-campaigns/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/their-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/their-campaigns/**").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER")
                .requestMatchers("/api/their-campaigns/import").hasAnyRole("ADMIN", "CAMPAIGN_MANAGER") // Импорт данных

                // Отчеты
                .requestMatchers("/api/reports/campaigns").hasAnyRole("ADMIN", "ANALYST")
                .requestMatchers("/api/reports/campaigns/**").hasAnyRole("ADMIN", "ANALYST")
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated())
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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
