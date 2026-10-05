package com.salohi.hrms;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByUsername(username)
            .map(u -> User.withUsername(u.getUsername()).password(u.getPassword())
                    .roles(u.getRole().name()).disabled(!u.isEnabled()).build())
            .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())   // stateless Basic-auth API
            .authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/employees/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
