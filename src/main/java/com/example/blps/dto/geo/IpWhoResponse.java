package com.example.blps.dto.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private Flag flag;
    private Connection connection;
    private Security security;
    private Currency currency;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Flag {
        private String img;
        private String emoji;
        @JsonProperty("emoji_unicode")
        private String emojiUnicode;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Connection {
        private Integer asn;
        private String org;
        private String isp;
        private String domain;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Security {
        private boolean anonymous;
        private boolean proxy;
        private boolean vpn;
        private boolean tor;
        private boolean hosting;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Currency {
        private String name;
        private String code;
        private String symbol;
        private String plural;
        @JsonProperty("exchange_rate")
        private Double exchangeRate;
    }
}