package com.sanisidro.restaurante.core.config;

import com.sanisidro.restaurante.core.security.jwt.JwtAuthFilter;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.service.GoogleUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleUserService googleUserService;

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
                            var oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
                            String email = oAuth2User.getAttribute("email");
                            String name = oAuth2User.getAttribute("name");

                            // 🔹 Buscar usuario en BD
                            UserDetails user;
                            try {
                                user = userDetailsService.loadUserByUsername(email);
                            } catch (UsernameNotFoundException e) {
                                // 🔹 Usuario no existe → crearlo automáticamente
                                user = googleUserService.createUserFromGoogle(email, name);
                            }

                            // 🔹 Generar JWT
                            Map<String, Object> extraClaims = Map.of(
                                    "roles", user.getAuthorities().stream().map(a -> a.getAuthority()).toList()
                            );
                            String accessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);

                            // 🔹 Redirigir al frontend con token
                            response.sendRedirect("http://localhost:4200/loginSuccess?token=" + accessToken);
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
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


