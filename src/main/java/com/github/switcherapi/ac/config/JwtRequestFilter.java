package com.github.switcherapi.ac.config;

import com.github.switcherapi.ac.config.SecurityConfig.Roles;
import com.github.switcherapi.ac.service.JwtTokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.*;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger jwtLogger = LogManager.getLogger(JwtRequestFilter.class);

    public static final String SWITCHER_AC = "SWITCHER_AC";

    public static final String BEARER = "Bearer ";

    private Map<Roles, SimpleGrantedAuthority> grantedAuthorities;

    private final JwtTokenService jwtTokenService;

    public JwtRequestFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
        this.setupComponent();
    }

    private void setupComponent() {
        final EnumMap<Roles, SimpleGrantedAuthority> roles = new EnumMap<>(Roles.class);
        roles.put(Roles.ROLE_SWITCHER, new SimpleGrantedAuthority(Roles.ROLE_SWITCHER.name()));
        roles.put(Roles.ROLE_ADMIN, new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name()));
        grantedAuthorities = Collections.unmodifiableMap(roles);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final Optional<String> jwt = getJwtFromRequest(request);

        jwt.ifPresent(token -> {
            if (jwtLogger.isDebugEnabled()) {
                jwtLogger.debug("Token {}", token);
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (validateToken(token, request, authorities)) {
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
    private boolean validateToken(String token, HttpServletRequest request,
                                  List<SimpleGrantedAuthority> authorities) {

        if (request.getRequestURI().startsWith("/switcher")) {
            if (jwtTokenService.validateRelayToken(token)) {
                return authorities.add(grantedAuthorities.get(Roles.ROLE_SWITCHER));
            }
        } else if (jwtTokenService.validateAdminToken(token)) {
            return authorities.add(grantedAuthorities.get(Roles.ROLE_ADMIN));
        }

        return false;
    }

    private static Optional<String> getJwtFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

}