package com.example.blps.model.geo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IpWhoResponse {
    private String ip;
    private boolean success;
    private String type;
    private String continent;
    @JsonProperty("continent_code")
    private String continentCode;
    private String country;
    @JsonProperty("country_code")
    private String countryCode;
    private String region;
    @JsonProperty("region_code")
    private String regionCode;
    private String city;
    private Double latitude;
    private Double longitude;
    @JsonProperty("is_eu")
    private boolean isEu;
    private String postal;
    @JsonProperty("calling_code")
    private String callingCode;
    private String capital;
    private String borders;
    
    // Nested objects
    private TimeZone timezone;
    
    @Data
    public static class TimeZone {
        private String id;
        private String abbr;
        @JsonProperty("is_dst")
        private boolean isDst;
        private Integer offset;
        private String utc;
        @JsonProperty("current_time")
        private String currentTime;
    }
}