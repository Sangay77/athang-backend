package com.bfs.rma.auth.jwt;

import com.bfs.rma.auth.service.CustomUserDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.info("🛡️ JWT received: {}", token);
            try {
                username = jwtService.extractUserName(token);
            } catch (ExpiredJwtException ex) {
                logger.warn("⚠️ JWT token expired: {}", ex.getMessage());
                sendErrorResponse(response, "JWT token expired", request.getServletPath());
                return; // stop filter chain here
            } catch (Exception e) {
                logger.warn("⚠️ JWT token invalid: {}", e.getMessage());
                sendErrorResponse(response, "Invalid JWT token", request.getServletPath());
                return; // stop filter chain here
            }
        } else {
            logger.warn("❌ No JWT token found in the request headers.");
            // Optional: You can choose to send an error response here if token is mandatory for all requests,
            // or just let it pass for public endpoints.
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = context.getBean(CustomUserDetailService.class).loadUserByUsername(username);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                logger.warn("⚠️ JWT token is invalid for user: {}", username);
                sendErrorResponse(response, "Invalid JWT token", request.getServletPath());
                return; // stop filter chain here
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("path", path);

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
