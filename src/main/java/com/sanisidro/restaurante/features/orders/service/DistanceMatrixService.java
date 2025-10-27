package com.sanisidro.restaurante.features.orders.service;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DistanceMatrixService {

    @Value("${google.maps.api-key}")
    private String apiKey;

    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    public Map<String, Object> getDistanceAndDuration(double originLat, double originLng,
                                                      double destinationLat, double destinationLng) {

        URI uri = UriComponentsBuilder.fromUriString(DISTANCE_MATRIX_URL)
                .queryParam("origins", originLat + "," + originLng)
                .queryParam("destinations", destinationLat + "," + destinationLng)
                .queryParam("key", apiKey)
                .queryParam("mode", "driving")
                .build()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(uri, Map.class);
    }

}
