package org.igniters.qa.sut.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads {@code Authorization: Bearer <token>} on API requests. Leaves the
 * security context empty (not an error) when the header is missing or the
 * token doesn't resolve — the security filter chain's entry point is what
 * turns "no authentication" into a 401 for endpoints that require one.
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AppUserDetailsService userDetailsService;

    public AuthTokenFilter(TokenService tokenService, AppUserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            tokenService.resolveEmail(token).ifPresent(email -> {
                try {
                    AppUserPrincipal principal = (AppUserPrincipal) userDetailsService.loadUserByUsername(email);
                    var authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UsernameNotFoundException ignored) {
                    // Token outlived the user record (shouldn't happen outside tests
                    // that delete data mid-run) — leave the request unauthenticated.
                }
            });
        }
        chain.doFilter(request, response);
    }
}
