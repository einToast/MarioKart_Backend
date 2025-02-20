package de.fsr.mariokart_backend.security;

import de.fsr.mariokart_backend.user.repository.UserRepository;
import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
@AllArgsConstructor
public class ApplicationSecurity {
    private final JwtTokenFilter jwtTokenFilter;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((auth) -> auth

                        .requestMatchers(HttpMethod.GET, "/settings").permitAll()
                        .requestMatchers(HttpMethod.POST, "/settings").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/settings").authenticated()

                        .requestMatchers(HttpMethod.GET, "/users").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/users/register/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/teams/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/teams").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/teams/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/teams/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/match_plan/rounds/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/match_plan/games/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/match_plan/points").permitAll()
                        .requestMatchers(HttpMethod.GET, "/match_plan/create/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/match_plan/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/match_plan/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/match_plan/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/survey").permitAll()
                        .requestMatchers(HttpMethod.GET, "/survey/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/survey/*/answers").permitAll()
                        .requestMatchers(HttpMethod.POST, "/survey").authenticated()
                        .requestMatchers(HttpMethod.POST, "/survey/answer").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/survey/*").authenticated()

                        .requestMatchers(HttpMethod.GET, "/healthcheck").permitAll()

                        .requestMatchers("/ws/**").permitAll()

                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
//                .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    UrlBasedCorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8100", "http://127.0.0.1:8100"));
//        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }



    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}