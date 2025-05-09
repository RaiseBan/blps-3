package com.example.blps.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String chartType; 
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private Map<String, Object> data;
    private Map<String, Object> options;
}