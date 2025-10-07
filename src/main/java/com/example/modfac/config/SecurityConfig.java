package com.example.modfac.config;

import com.example.modfac.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    /**
     * Configuration class for setting up application security.
     * <p>
     * This class configures security settings such as CORS, CSRF, session management,
     * and request authorization. It also integrates a JWT authentication filter
     * to handle token-based authentication.
     * </p>
     * <p>
     * The class uses Spring Security annotations to enable web security and method-level
     * security. It defines a security filter chain and a password encoder bean for
     * securing the application.
     * </p>
     */

    public static final String ADMIN_ROLE = "ADMIN";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
         * Configures the security filter chain for the application.
         * <p>
         * This method sets up the security settings, including disabling CSRF,
         * configuring CORS, setting the session management policy to stateless,
         * and defining authorization rules for HTTP requests. It also integrates
         * a JWT authentication filter into the filter chain.
         * </p>
         *
         * @param http the {@link HttpSecurity} object used to configure security settings.
         * @return the configured {@link SecurityFilterChain}.
         * @throws Exception if an error occurs while configuring the security settings.
         */

    /**
         * Provides a password encoder bean for encoding passwords.
         * <p>
         * This method returns an instance of {@link BCryptPasswordEncoder},
         * which is a secure implementation of password encoding using the BCrypt hashing function.
         * </p>
         *
         * @return a {@link PasswordEncoder} instance for encoding passwords.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

    /**
         * Configures the CORS settings for the application.
         * <p>
         * This method defines the allowed origins, HTTP methods, and headers for CORS requests.
         * It registers these configurations for all endpoints in the application.
         * </p>
         *
         * @return a {@link CorsConfigurationSource} instance with the configured CORS settings.
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of("*"));
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
}