package com.sanisidro.restaurante.core.security.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OAuth2Controller {

    @GetMapping("/loginSuccess")
    public Map<String, Object> loginSuccess(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            return Map.of("success", false, "message", "No authentication", "data", null);
        }
        var principal = authentication.getPrincipal();
        Map<String, Object> userDetails = Map.of(
                "name", principal.getAttribute("name"),
                "email", principal.getAttribute("email"),
                "picture", principal.getAttribute("picture")
        );
        return Map.of("success", true, "message", "Login successful", "data", userDetails);
    }

    @GetMapping("/loginFailure")
    public Map<String, Object> loginFailure() {
        return Map.of("success", false, "message", "Login failed", "data", null);
    }
}