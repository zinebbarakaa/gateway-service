package com.giantLink.Gateway.filter;

import com.giantLink.Gateway.utilities.RouteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    RouteValidator routeValidator;
    @Autowired
    RestTemplate restTemplate;
    static final String url="http://localhost:8080/auth/validate-token";
    String token="";

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(AuthenticationFilter.Config config) {
        return (((exchange, chain) -> {
            // Check for the URL doesn't match the authorized endpoints defined in the RoutValidator
            if (routeValidator.enableFilter.test(exchange.getRequest())){
                // Check if the token is present in the AUTH header of the request
                if(exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    String requestHeader= Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
                    System.out.println(requestHeader);
                    if (requestHeader!=null && requestHeader.startsWith("Bearer")){
                        try {
                            // Validate the token via security-service using rest template
                            token=requestHeader.substring(7);
                            HttpHeaders headers = new HttpHeaders();
                            headers.set("token", token);
                            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                            Boolean response = restTemplate.postForObject(url, requestEntity, Boolean.class);
                            if(!response){
                                throw new RuntimeException("Access denied token not valid");
                            }
                            //Boolean response =restTemplate.postForObject(url,"",Boolean.class);
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                            throw new RuntimeException("Access denied token not valid");
                        }

                    }

                }else {
                    throw new RuntimeException("Token missed");
                }
            }
            return chain.filter(exchange);
        }));
    }
}
