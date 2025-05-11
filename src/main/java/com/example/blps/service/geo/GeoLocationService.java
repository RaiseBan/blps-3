package com.example.blps.service.geo;

import com.example.blps.dto.geo.IpWhoResponse;
import com.example.blps.model.geo.GeoLocationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationService {

    private static final String IP_WHO_API_URL = "http://ipwho.is/";
    private final RestTemplate restTemplate;

    public GeoLocationData getGeoData(String ip, String referralHash, Long campaignId) {
        try {
            log.debug("Fetching geo data for IP: {}", ip);

            if (ip.equals("127.0.0.1") || ip.equals("localhost")) {
                ip = "0:0:0:0:0:0:0:1";
                log.info("Replaced localhost IP with IPv6 format: {}", ip);
            }

            String url = IP_WHO_API_URL + ip;
            IpWhoResponse response = restTemplate.getForObject(url, IpWhoResponse.class);

            if (response != null) {
                log.info("Got response from IP service: {}", response);

                if (response.isSuccess()) {
                    return GeoLocationData.builder()
                            .ip(ip)
                            .country(response.getCountry())
                            .countryCode(response.getCountryCode())
                            .region(response.getRegion())
                            .regionCode(response.getRegionCode())
                            .city(response.getCity())
                            .latitude(response.getLatitude())
                            .longitude(response.getLongitude())
                            .continent(response.getContinent())
                            .continentCode(response.getContinentCode())
                            .timezone(response.getTimezone() != null ? response.getTimezone().getId() : null)
                            .timestamp(LocalDateTime.now())
                            .referralHash(referralHash)
                            .campaignId(campaignId)
                            .clickCount(1)
                            .build();
                } else {
                    log.error("API returned unsuccessful response for IP: {}. Response: {}", ip, response);
                    return createDefaultGeoData(ip, referralHash, campaignId);
                }
            } else {
                log.error("No response from IP service for IP: {}", ip);
                return createDefaultGeoData(ip, referralHash, campaignId);
            }
        } catch (Exception e) {
            log.error("Error fetching geo data for IP: {}", ip, e);
            return createDefaultGeoData(ip, referralHash, campaignId);
        }
    }

    private GeoLocationData createDefaultGeoData(String ip, String referralHash, Long campaignId) {
        return GeoLocationData.builder()
                .ip(ip)
                .country("Unknown")
                .countryCode("XX")
                .timestamp(LocalDateTime.now())
                .referralHash(referralHash)
                .campaignId(campaignId)
                .clickCount(1)
                .build();
    }
}