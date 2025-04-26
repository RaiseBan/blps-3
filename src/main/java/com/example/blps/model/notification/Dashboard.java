package com.example.blps.model.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dashboard")
public class Dashboard {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(
            name = "uuid2",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    // ID для совместимости с предыдущим кодом
    @Transient // Не сохраняем в БД
    private Long numericId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "chart_data", columnDefinition = "TEXT")
    private String chartData;

    @Column(name = "chart_config", columnDefinition = "TEXT")
    private String chartConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DashboardType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Геттер для возврата Long ID для обратной совместимости
    public Long getId() {
        if (id == null) {
            return null;
        }

        if (numericId == null) {
            // Создаем числовой ID из UUID как хеш-значение
            numericId = Math.abs(id.hashCode()) % 1_000_000_000L;
        }

        return numericId;
    }

    // Сеттер для обратной совместимости
    public void setId(Long id) {
        this.numericId = id;
    }

    // Геттер для UUID
    public UUID getUuid() {
        return id;
    }
}