package com.example.EDM.Filter;

import com.example.EDM.Dto.TokenValidationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;  // ✅ was missing
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class AuthFilterChain extends OncePerRequestFilter {

    private RestTemplate restTemplate;

    public AuthFilterChain(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("entering point");

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = null;
        for (Cookie c : cookies) {
            if (c.getName().equals("JWT_TOKEN")) {
                token = c.getValue();
                System.out.println("token"+token);
                break;
            }
        }

        if (token == null) {
            logger.info("token null");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // ✅ Declare outside try so it's accessible after
        TokenValidationResponse validationResponse = null;

        try {
            validationResponse = restTemplate.exchange(
                    "http://localhost:8080/api/v1/auth/validate-token",
                    HttpMethod.POST,
                    entity,
                    TokenValidationResponse.class
            ).getBody();

        } catch (HttpClientErrorException e) {
            System.out.println("service is not authorised");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;

        } catch (HttpServerErrorException e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;

        } catch (ResourceAccessException e) {
            throw e;
        }

        // ✅ Null and valid check outside try
        if (validationResponse == null || !validationResponse.isValid()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        System.out.println("Roles for the current user"+validationResponse.getRoles());

        // ✅ Build authToken here where validationResponse is accessible
        List<SimpleGrantedAuthority> authorities = validationResponse.getRoles()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        validationResponse.getUsername(),
                        null,
                        authorities
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}