package ru.prodcontest.security;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.prodcontest.service.JwtService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HEADER_NAME);
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());
        String login = jwtService.getUsernameFromToken(jwt);
        String password = jwtService.getPasswordFromToken(jwt);
        if (login == null || password == null) {
            filterChain.doFilter(request, response);
        }

        if (StringUtils.isNotEmpty(login) && StringUtils.isNotEmpty(password)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                login,
                password
            );

            authenticationToken.setDetails(new WebAuthenticationDetails(request));
            try {
                Authentication authentication = authenticationConfiguration.getAuthenticationManager().authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                filterChain.doFilter(request, response);
            }
        }

        filterChain.doFilter(request, response);
    }
}
