package com.example.blps.model.geo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String ip;
    private String country;
    private String countryCode;
    private String region;
    private String regionCode;
    private String city;
    private Double latitude;
    private Double longitude;
    private String continent;
    private String continentCode;
    private String timezone;
    private LocalDateTime timestamp;
    private String referralHash; 
    private Long campaignId; 
    private Integer clickCount = 1; 
}