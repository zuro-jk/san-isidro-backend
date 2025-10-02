package com.sanisidro.restaurante.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.core.security.dto.AuthResponse;
import com.sanisidro.restaurante.core.security.service.OAuthUserService;
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
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthToken.getPrincipal();

        String providerId = provider.equalsIgnoreCase("google") ? oauthUser.getAttribute("sub") : oauthUser.getAttribute("id");
        String email = oauthUser.getAttribute("email");
        String firstName = oauthUser.getAttribute("given_name");
        String lastName = oauthUser.getAttribute("family_name");
        Boolean emailVerified = provider.equalsIgnoreCase("google") ? oauthUser.getAttribute("email_verified") : false;
        String profileImageUrl = oauthUser.getAttribute("picture");

        if (provider.equalsIgnoreCase("google") && (emailVerified == null || !emailVerified)) {
            log.warn("Usuario Google no verificado: {}", email);
            response.sendRedirect("/loginFailure?error=email_not_verified");
            return;
        }

        AuthResponse authResponse = oAuthUserService.processOAuthUser(
                provider, providerId, email, firstName, lastName, emailVerified, profileImageUrl
        );

        log.info("OAuth2 login successful: provider={}, email={}, providerId={}", provider, email, providerId);

        String userJson = objectMapper.writeValueAsString(authResponse.getUser());

        String script = """
            <html><body><script>
                try {
                    console.log('Enviando datos al frontend...');
                    window.opener.postMessage(
                        {
                            success: true,
                            accessToken: '%s',
                            refreshToken: '%s',
                            user: %s
                        },
                        'http://localhost:4200'
                    );
                    console.log('Datos enviados correctamente');
                } catch(e) {
                    console.error('Error enviando postMessage:', e);
                }
                window.close();
            </script></body></html>
        """.formatted(authResponse.getAccessToken(), authResponse.getSessionId(), userJson);

        response.setContentType("text/html");
        response.getWriter().write(script);
    }
}