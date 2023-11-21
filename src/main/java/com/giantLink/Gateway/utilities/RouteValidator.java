package com.giantLink.Gateway.utilities;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> exposedEndpoints=List.of(
            "/auth/register",
            "/auth/login",
            "/auth/validate-token",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> enableFilter= request -> exposedEndpoints
            .stream()
            .noneMatch(url -> request.getURI().getPath().contains(url));
}
