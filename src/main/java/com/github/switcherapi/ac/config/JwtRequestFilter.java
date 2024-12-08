package com.github.switcherapi.ac.config;

import com.github.switcherapi.ac.config.SecurityConfig.Roles;
import com.github.switcherapi.ac.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    public static final String SWITCHER_AC = "SWITCHER_AC";
    public static final String BEARER = "Bearer ";

    private Map<Roles, SimpleGrantedAuthority> grantedAuthorities;

    private final JwtTokenService jwtTokenService;

    public JwtRequestFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
        this.setupComponent();
    }

    private void setupComponent() {
        final var roles = new EnumMap<Roles, SimpleGrantedAuthority>(Roles.class);
        roles.put(Roles.ROLE_SWITCHER, new SimpleGrantedAuthority(Roles.ROLE_SWITCHER.name()));
        roles.put(Roles.ROLE_ADMIN, new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name()));
        grantedAuthorities = Collections.unmodifiableMap(roles);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final var jwt = getJwtFromRequest(request);

        jwt.ifPresent(token -> {
            log.debug("Token {}", token);

            var authorities = validateToken(token, request);
            if (!authorities.isEmpty()) {
                final var authUser = new UsernamePasswordAuthenticationToken(SWITCHER_AC, null, authorities);
                authUser.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authUser);
            }
        });

        filterChain.doFilter(request, response);
    }

    /**
     * Validate token given the accessed resource
     */
    private List<SimpleGrantedAuthority> validateToken(String token, HttpServletRequest request) {
        final var authorities = new ArrayList<SimpleGrantedAuthority>();
        if (request.getRequestURI().startsWith("/switcher")) {
            if (jwtTokenService.validateRelayToken(token)) {
                authorities.add(grantedAuthorities.get(Roles.ROLE_SWITCHER));
            }
        } else if (jwtTokenService.validateAdminToken(token)) {
            authorities.add(grantedAuthorities.get(Roles.ROLE_ADMIN));
        }

        return authorities;
    }

    private static Optional<String> getJwtFromRequest(HttpServletRequest request) {
        final var bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return Optional.of(bearerToken.substring(7));
        }

        return Optional.empty();
    }

}