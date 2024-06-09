package de.fsr.mariokart_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .requestMatchers("/**").permitAll() // Erlaubt den Zugriff auf alle Endpunkte
                .and()
                .csrf().disable(); // Deaktiviert CSRF-Schutz für REST-Anfragen
        return http.build();
    }
}
