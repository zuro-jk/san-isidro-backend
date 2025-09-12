package com.sanisidro.restaurante.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.core.security.dto.AuthResponse;
import com.sanisidro.restaurante.core.security.service.OAuthUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserService oAuthUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthToken.getPrincipal();

        String providerId = switch (provider.toLowerCase()) {
            case "google" -> oauthUser.getAttribute("sub");
            case "facebook" -> oauthUser.getAttribute("id");
            case "github" -> oauthUser.getAttribute("id");
            default -> null;
        };

        String email = oauthUser.getAttribute("email");
        String firstName = oauthUser.getAttribute("given_name");
        String lastName = oauthUser.getAttribute("family_name");

        Boolean emailVerified = provider.equals("google") ? oauthUser.getAttribute("email_verified") : false;

        if (provider.equals("google") && (emailVerified == null || !emailVerified)) {
            log.warn("OAuth2 login failed for Google user (email not verified): {}", email);
            response.sendRedirect("/loginFailure?error=email_not_verified");
            return;
        }

        log.info("OAuth2 login success: provider={}, email={}, providerId={}", provider, email, providerId);

        // Procesar usuario y generar respuesta con tokens
        AuthResponse authResponse = oAuthUserService.processOAuthUser(
                provider, providerId, email, firstName, lastName, emailVerified
        );

        // Enviar datos al frontend via ventana emergente
        String script = """
            <html><body><script>
                window.opener.postMessage(
                    {
                        accessToken: '%s',
                        refreshToken: '%s',
                        user: %s
                    },
                    'http://localhost:4200'
                );
                window.close();
            </script></body></html>
            """.formatted(
                authResponse.getAccessToken(),
                authResponse.getRefreshToken(),
                objectMapper.writeValueAsString(authResponse.getUser())
        );

        response.setContentType("text/html");
        response.getWriter().write(script);
    }
}