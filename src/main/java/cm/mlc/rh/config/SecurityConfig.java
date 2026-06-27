package cm.mlc.rh.config;

import cm.mlc.rh.security.JwtAuthFilter;
import cm.mlc.rh.security.JwtTokenProvider;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Deux chaînes de sécurité :
 *  - API /api/**  : sans état, authentification JWT (Bearer).
 *  - Web (reste)  : session Spring Security, formulaire de login, CSRF actif.
 */
@Configuration
@EnableMethodSecurity   // active @PreAuthorize (principe du moindre privilège)
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean @Order(1)
    SecurityFilterChain apiChain(HttpSecurity http, JwtTokenProvider provider) throws Exception {
        http.securityMatcher("/api/**")
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/v1/auth/login").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(provider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean @Order(2)
    SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/css/**", "/static/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?deconnecte").permitAll())
            // En-têtes de sécurité (défense en profondeur) : anti-clickjacking,
            // anti-sniffing MIME, et HSTS pour forcer HTTPS côté navigateur.
            .headers(h -> h
                .frameOptions(f -> f.sameOrigin())
                .contentTypeOptions(c -> {})
                .httpStrictTransportSecurity(s -> s
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000))
                .referrerPolicy(r -> r.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));
        return http.build();
    }
}
