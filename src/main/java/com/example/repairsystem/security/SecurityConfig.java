package com.example.repairsystem.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/auth/me").authenticated()

                // Заявки
                .requestMatchers(HttpMethod.POST,   "/api/requests").hasAnyAuthority("ROLE_CREATOR","ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/requests/*/accept").hasAnyAuthority("ROLE_WORKER","ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/requests/*/close").hasAnyAuthority("ROLE_CLOSER","ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/requests/*/change-service").hasAnyAuthority("ROLE_WORKER","ROLE_CLOSER","ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/requests/*/reopen").hasAnyAuthority("ROLE_CLOSER","ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/requests/*/confirm").hasAnyAuthority("ROLE_CONFIRMER","ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/requests/**").hasAnyAuthority("ROLE_DELETER","ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/requests/**").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/requests/*/history").authenticated()

                // Оборудование и файлы
                .requestMatchers("/api/equipment/**").authenticated()

                // Справочники корпусов и участков — все авторизованные
                .requestMatchers("/api/buildings/**").authenticated()
                .requestMatchers("/api/sections/**").authenticated()

                // SMS справочники — только ADMIN
                .requestMatchers("/api/sms/**").hasAuthority("ROLE_ADMIN")

                // Настройки пользователя
                .requestMatchers("/api/settings/**").authenticated()

                // Управление пользователями
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Не авторизован\"}");
                    },
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/**")
                )
            )
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }
}
