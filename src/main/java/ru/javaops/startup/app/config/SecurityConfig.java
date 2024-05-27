package ru.javaops.startup.app.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import ru.javaops.startup.user.model.Role;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
@AllArgsConstructor
public class SecurityConfig {
    public static final String API_PATH = "/api";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                        .requestMatchers(API_PATH + "/admin/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").hasRole(Role.ADMIN.name())
                        .anyRequest().permitAll())
                .formLogin(withDefaults())
                .httpBasic(withDefaults())
                .logout(lc -> lc.logoutSuccessUrl("/"))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}