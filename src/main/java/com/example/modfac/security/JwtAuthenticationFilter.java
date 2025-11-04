package com.example.modfac.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
    
        LOG.debug("doFilterInternal method invoked");
    
        try {
            String jwt = getJwtFromRequest(request);
    
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsername(jwt);
                String role = tokenProvider.getRole(jwt);
    
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (StringUtils.hasText(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }
    
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities);
    
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // If we reach here, it means the JWT token is invalid or missing
                // So we set the WWW-Authenticate header and throw a 401 response
                response.setHeader("WWW-Authenticate", "Bearer");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception ex) {
            LOG.error("Could not set user authentication in security context", ex);
        }
    
        filterChain.doFilter(request, response);
    
        LOG.debug("doFilterInternal method finished");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        LOG.debug("shouldNotFilter method invoked");
    
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
    
        boolean shouldNotFilter = !(PATH_MATCHER.match("/api/employees/**", path) ||
                PATH_MATCHER.match("/auth/register/**", path));
    
        LOG.debug("shouldNotFilter method finished");
        return shouldNotFilter;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        LOGGER.debug("getJwtFromRequest method invoked");
    
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
    
        LOGGER.debug("getJwtFromRequest method finished");
        return null;
    }
}
