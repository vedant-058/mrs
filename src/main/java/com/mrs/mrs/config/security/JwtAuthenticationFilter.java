package com.mrs.mrs.config.security;

import com.mrs.mrs.exception.AuthenticationException;
import com.mrs.mrs.service.TokenService;
import com.mrs.mrs.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final TokenService tokenService;
    private final UserService userService;

    // Constructor Injection
    public JwtAuthenticationFilter(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        
        // Inside your doFilterInternal method
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // This logs to your IntelliJ/Terminal console
            System.out.println("ALERT: Unauthorized access attempt! To access this resource, please login and provide a JWT token.");
            
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        
        // Check if token is blacklisted first (before any parsing)
        if (tokenService.isTokenBlacklisted(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token has been invalidated\"}");
            response.setContentType("application/json");
            return;
        }
        
        try {
            // Validate token first (checks blacklist and signature/expiration)
            if (!tokenService.validateToken(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }
            
            // Extract userId from token (will throw exception if invalid)
            UUID userId = tokenService.getUserIdFromToken(jwt);
            
            // Set authentication if not already set
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserById(userId);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (AuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            // Escape JSON special characters in error message
            String escapedMessage = e.getMessage().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
            response.getWriter().write("{\"error\":\"" + escapedMessage + "\"}");
            return;
        } catch (Exception e) {
            System.err.println("JWT Validation Error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}