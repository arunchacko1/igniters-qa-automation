package org.igniters.qa.sut.config;

import org.igniters.qa.sut.security.AppUserDetailsService;
import org.igniters.qa.sut.security.AuthTokenFilter;
import org.igniters.qa.sut.security.RestAccessDeniedHandler;
import org.igniters.qa.sut.security.RestAuthEntryPoint;
import org.igniters.qa.sut.security.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Two independent filter chains: a stateless, token-based one for
 * {@code /api/**} (so API clients never need cookies or CSRF tokens), and a
 * session-based one with form login for the Thymeleaf pages. Spring
 * Security picks whichever chain's {@code securityMatcher} matches the
 * request, in {@code @Order}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            TokenService tokenService,
            AppUserDetailsService userDetailsService,
            RestAuthEntryPoint authEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Rules are matched in order, first match wins — so the
                        // narrower "/registrations" patterns must come before the
                        // broader "/api/events/**" ones they'd otherwise be shadowed by.
                        .requestMatchers(HttpMethod.POST, "/api/events/*/registrations").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/*/registrations").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.POST, "/api/events").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/events/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new AuthTokenFilter(tokenService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/events", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }
}
