package com.sanisidro.restaurante.core.config;

import com.sanisidro.restaurante.core.security.jwt.JwtAuthFilter;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.service.OAuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OAuthUserService oAuthUserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.setAllowedOrigins(List.of("http://localhost:4200"));
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/loginSuccess").permitAll()
                        .requestMatchers("/api/v1/customers/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/v1/addresses/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/v1/reservations/**").hasAnyRole("CLIENT", "WAITER", "ADMIN")
                        .requestMatchers("/api/v1/reviews/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/v1/orders/**").hasAnyRole("WAITER", "CHEF", "CASHIER", "ADMIN")
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "CHEF")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                            String provider = oauthToken.getAuthorizedClientRegistrationId();
                            OAuth2User oauthUser = oauthToken.getPrincipal();

                            // Obtener ID del proveedor
                            String providerId = switch (provider.toLowerCase()) {
                                case "google" -> oauthUser.getAttribute("sub");
                                case "facebook" -> oauthUser.getAttribute("id");
                                case "github" -> oauthUser.getAttribute("id");
                                default -> null;
                            };

                            String email = oauthUser.getAttribute("email");
                            String firstName = oauthUser.getAttribute("given_name");
                            String lastName = oauthUser.getAttribute("family_name");

                            // Validar si el correo está verificado (solo Google garantiza esto)
                            Boolean emailVerified = provider.equals("google") ? oauthUser.getAttribute("email_verified") : false;

                            if (provider.equals("google") && (emailVerified == null || !emailVerified)) {
                                response.sendRedirect("/loginFailure");
                                return;
                            }

                            String jwt = oAuthUserService.processOAuthUser(provider, providerId, email, firstName, lastName, emailVerified);
                            response.sendRedirect("http://localhost:8080/loginSuccess?token=" + jwt);
                        })
                        .failureHandler((request, response, exception) -> {
                            System.out.println("Login ERROR! Exception: " + exception.getMessage());
                            response.sendRedirect("/loginFailure");
                        })
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        CustomDaoAuthenticationProvider authProvider = new CustomDaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


