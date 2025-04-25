package com.example.blps.model.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Dashboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String chartData;

    @Column(columnDefinition = "TEXT")
    private String chartConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DashboardType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isPublished = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}