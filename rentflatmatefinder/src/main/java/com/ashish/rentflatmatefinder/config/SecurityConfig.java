package com.ashish.rentflatmatefinder.config;

import com.ashish.rentflatmatefinder.security.JwtAuthenticationEntryPoint;
import com.ashish.rentflatmatefinder.security.filter.JwtAuthenticationFilter;
import com.ashish.rentflatmatefinder.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfig corsConfig;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/ws/**",
                                "/uploads/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/listings/my", "/api/v1/listings/*/fill").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/api/v1/listings/*/compatibility").hasAnyRole("TENANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/listings", "/api/v1/listings/*")
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/tenant-profile/**").hasAnyRole("TENANT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/listings").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/listings/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/listings/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/interests").hasAnyRole("TENANT", "ADMIN")
                        .requestMatchers("/api/v1/interests/sent").hasAnyRole("TENANT", "ADMIN")
                        .requestMatchers("/api/v1/interests/received", "/api/v1/interests/*/accept", "/api/v1/interests/*/decline")
                        .hasAnyRole("OWNER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
