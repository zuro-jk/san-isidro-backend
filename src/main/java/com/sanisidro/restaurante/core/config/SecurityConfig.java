package com.sanisidro.restaurante.core.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.sanisidro.restaurante.core.security.handler.OAuth2FailureHandler;
import com.sanisidro.restaurante.core.security.handler.OAuth2SuccessHandler;
import com.sanisidro.restaurante.core.security.jwt.JwtAuthFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

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
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/loginSuccess").permitAll()
                        .requestMatchers("/api/v1/notification/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "CHEF", "MANAGER")
                        .requestMatchers("/api/v1/reviews/**").hasAnyRole("CLIENT", "ADMIN", "MANAGER")

                        .requestMatchers("/api/v1/staff/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/suppliers/**").hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/promotions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/promotions/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/promotions/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/promotions/**").hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers("/api/v1/loyalty-management/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/feedback-and-loyalty/**").hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/delivery/**").hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers("/api/v1/users/**")
                        .hasAnyRole("CLIENT", "ADMIN", "WAITER", "CASHIER", "MANAGER")
                        .requestMatchers("/api/v1/customers/**")
                        .hasAnyRole("CLIENT", "ADMIN", "WAITER", "CASHIER", "MANAGER")
                        .requestMatchers("/api/v1/addresses/**")
                        .hasAnyRole("CLIENT", "ADMIN", "WAITER", "CASHIER", "MANAGER")

                        .requestMatchers("/api/v1/reservations/**").hasAnyRole("CLIENT", "WAITER", "ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/orders/**")
                        .hasAnyRole("CLIENT", "WAITER", "CHEF", "CASHIER", "ADMIN", "MANAGER")

                        .requestMatchers("/api/v1/stores/**").hasAnyRole("CLIENT", "ADMIN")

                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        }))
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
    public AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
