package org.example.itemcounting.JWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.example.itemcounting.business.service.JwtService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    Claims claims = jwtService.extractAllClaims(token);
                    String username = claims.getSubject();
                    String group = claims.get("group", String.class);
                    Long userId = claims.get("userId", Long.class);
                    Long warehouseId = claims.get("warehouseId", Long.class);

                    if (username != null && group != null && userId != null) {
                        List<GrantedAuthority> authorities =
                                Collections.singletonList(new SimpleGrantedAuthority(group));

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        new AuthenticatedUser(userId, username, warehouseId), null, authorities);
                    
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (JwtException | IllegalArgumentException ex) {
                    SecurityContextHolder.clearContext();
                }
            }
        }
        chain.doFilter(request, response);
    }
}
