package com.plr.backend.security;

import com.plr.backend.model.User;
import com.plr.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// BUG-17 FIX: Tambah @Component agar Spring mengelola bean ini secara penuh
// sehingga @Autowired di dalam filter berfungsi dengan benar
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan ID: " + userId));
                    logger.debug("[JWT] Token valid for user: {} (ID: {})", user.getUsername(), userId);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities()
                        );
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("[JWT] Authentication set for user: {} (ID: {})", user.getUsername(), userId);
                } else {
                    logger.warn("[JWT] Token validation failed for URI: {}", request.getRequestURI());
                }
            } else {
                logger.debug("[JWT] No token in Authorization header for {} {}", request.getMethod(), request.getRequestURI());
            }
        } catch (Exception ex) {
            logger.error("[JWT] Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7).trim();
            logger.debug("[JWT] Raw token extracted, length={}", jwt.length());
            return jwt;
        }
        logger.debug("[JWT] No Authorization header or invalid format");
        return null;
    }
}
