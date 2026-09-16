package org.example.itemcounting.config;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.JWT.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты
                        .requestMatchers("/api/test/token").permitAll()
                        .requestMatchers("/status").permitAll()

                        // Products - чтение доступно всем авторизованным
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").authenticated()
                        // Создание/обновление/удаление продуктов - только ADMIN и MANAGER
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyAuthority("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyAuthority("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAuthority("ADMIN")

                        // Stock - чтение доступно всем авторизованным
                        .requestMatchers("/api/v1/stock/**").authenticated()

                        // Invoices - просмотр доступен всем авторизованным
                        .requestMatchers(HttpMethod.GET, "/api/v1/invoices/**").authenticated()
                        // Создание приходных накладных - ADMIN, MANAGER, WAREHOUSE_WORKER
                        .requestMatchers(HttpMethod.POST, "/api/v1/invoices/arrival").hasAnyAuthority("ADMIN", "MANAGER", "WAREHOUSE_WORKER")
                        // Создание расходных накладных - только ADMIN и MANAGER
                        .requestMatchers(HttpMethod.POST, "/api/v1/invoices/shipment").hasAnyAuthority("ADMIN", "MANAGER")
                        // Отмена накладных - только ADMIN и MANAGER
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/invoices/*/cancel").hasAnyAuthority("ADMIN", "MANAGER")

                        // View controllers - доступны всем авторизованным (если есть)
                        .requestMatchers("/view/**").authenticated()

                        // Остальные запросы требуют аутентификации
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied")))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
