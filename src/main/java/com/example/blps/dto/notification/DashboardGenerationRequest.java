package com.example.blps.dto.notification;

import com.example.blps.model.notification.DashboardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardGenerationRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private DashboardType type;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean autoPublish;
    private String createdBy;
    private String recipientsGroup;
}