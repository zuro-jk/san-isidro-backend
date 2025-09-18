package com.sanisidro.restaurante.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage = exception.getMessage() != null ? exception.getMessage() : "unknown_error";

        // 🚨 Log del fallo
        log.error("❌ OAuth2 login failed: {}", errorMessage, exception);

        // Crear objeto de error
        Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "OAuth2 login failed: " + errorMessage,
                "data", null
        );

        // 🚀 Enviar mensaje de error al frontend (popup -> opener)
        String script = """
            <html><body><script>
                window.opener.postMessage(
                    %s,
                    'http://localhost:4200'
                );
                window.close();
            </script></body></html>
            """.formatted(objectMapper.writeValueAsString(errorResponse));

        response.setContentType("text/html");
        response.getWriter().write(script);
    }
}
