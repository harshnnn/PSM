package com.example.gateway;

import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/auth/login",
            "/auth/register",
            "/h2/",
            "/actuator/"
    );

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!requiresAuthentication(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtTokenService.parseClaims(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if (username == null || username.isBlank() || role == null || role.isBlank()) {
                return unauthorized(exchange, "Invalid token payload");
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Role");
                        headers.remove("X-Username");
                        headers.set("X-User-Role", role);
                        headers.set("X-Username", username);
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean requiresAuthentication(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        if (path.startsWith("/api/")) {
            return true;
        }

        if (path.startsWith("/auth/profile/")) {
            return true;
        }

        return PUBLIC_PATH_PREFIXES.stream().noneMatch(path::startsWith) && path.startsWith("/auth/");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("X-Auth-Error", message);
        return exchange.getResponse().setComplete();
    }
}
