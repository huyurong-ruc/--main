package edu.ruc.platform.auth.security;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ruc.platform.auth.service.TokenService;
import edu.ruc.platform.auth.service.TokenBlocklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final TokenBlocklistService tokenBlocklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    token = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    break;
                }
            }
        }
        if (token != null && !token.isBlank()) {
            try {
                if (tokenBlocklistService.isRevoked(token)) {
                    throw new BusinessException("登录令牌已失效");
                }
                AuthenticatedUser user = tokenService.parseToken(token);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BusinessException ex) {
                SecurityContextHolder.clearContext();
                String uri = request.getRequestURI();
                if (uri != null && uri.startsWith("/api/")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ex.getMessage()));
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
