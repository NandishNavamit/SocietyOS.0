package com.database.societyproject.security.config;

import com.database.societyproject.security.service.*;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserSocietyRoleRepository roles;
    public JwtAuthenticationFilter(JwtService jwtService, UserSocietyRoleRepository roles) {
        this.jwtService = jwtService;
        this.roles = roles;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                JwtService.TokenData token = jwtService.parse(header.substring(7), "access");
                // Do not trust role claims that may have become stale after an admin
                // changes a user's access. The JWT proves identity; active database
                // roles define authorization for this request.
                var authorities = roles.findByUserIdAndActiveTrue(token.userId()).stream()
                        .map(role -> role.getRole())
                        .filter(role -> role.isCurrentProductRole())
                        .distinct()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList();
                UserPrincipal principal = new UserPrincipal(token.userId(), token.email());
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, authorities));
            } catch (RuntimeException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
