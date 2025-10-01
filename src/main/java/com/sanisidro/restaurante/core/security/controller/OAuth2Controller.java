package com.sanisidro.restaurante.core.security.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
public class OAuth2Controller {

    @GetMapping("/loginSuccess")
    public ResponseEntity<Map<String, Object>> loginSuccess(
            @RequestParam("token") String token,
            @RequestParam(value = "mode", defaultValue = "popup") String mode,
            HttpServletResponse response
    ) throws IOException {
        if ("popup".equalsIgnoreCase(mode)) {
            String html = "<html><body><script>" +
                    "window.opener.postMessage({ accessToken: '" + token + "' }, 'http://localhost:4200');" +
                    "window.close();" +
                    "</script></body></html>";
            response.setContentType("text/html");
            response.getWriter().write(html);
            return null;
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "token", token
        ));
    }

    @GetMapping("/loginFailure")
    public ResponseEntity<Map<String, Object>> loginFailure(
            @RequestParam(value = "error", required = false) String error) {
        log.warn("OAuth2 login failed: {}", error != null ? error : "Unknown reason");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "success", false,
                        "error", error != null ? error : "unknown_error",
                        "message", "Login failed"
                ));
    }

}