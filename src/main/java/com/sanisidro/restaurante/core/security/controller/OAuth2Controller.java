package com.sanisidro.restaurante.core.security.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class OAuth2Controller {

    @GetMapping("/loginSuccess")
    public void loginSuccess(HttpServletResponse response, @RequestParam("token") String token) throws IOException {
        // Esto va en tu controller que maneja /loginSuccess
        String html = "<html><body><script>" +
                "window.opener.postMessage({ accessToken: '" + token + "' }, 'http://localhost:4200');" +
                "window.close();" +
                "</script></body></html>";
        response.setContentType("text/html");
        response.getWriter().write(html);
    }

    @GetMapping("/loginFailure")
    public ResponseEntity<Map<String, Object>> loginFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Login failed", "data", null));
    }
}