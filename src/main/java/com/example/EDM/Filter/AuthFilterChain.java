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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;  // ✅ was missing
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        String token=null;
        for(Cookie c:request.getCookies()){
            if(c.getName().equals("JWT_TOKEN")){
                token=c.getValue();
                break;
            }
        }
        if(token==null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE,"JWT_TOKEN="+token);
        HttpEntity<?> httpEntity=new HttpEntity<>(httpHeaders);

        TokenValidationResponse tokenValidationResponse=null;
        try{
            tokenValidationResponse=restTemplate.exchange("http://localhost:8080/api/v1/auth/validate-token",
                    HttpMethod.POST,httpEntity,TokenValidationResponse.class).getBody();


        }
        catch (HttpClientErrorException e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        catch (HttpServerErrorException e){
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        if(tokenValidationResponse==null||!tokenValidationResponse.isValid()){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        System.out.println("Roles"+tokenValidationResponse.getRoles());
        Set<GrantedAuthority> authorities=tokenValidationResponse.getRoles()
                .stream().map(r->new SimpleGrantedAuthority(r)).collect(Collectors.toSet());



        UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(tokenValidationResponse.getUsername(),null,authorities);

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}