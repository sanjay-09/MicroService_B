package com.example.EDM.Config;


import com.example.EDM.Filter.AuthFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private AuthFilterChain authFilterChain;

    public SecurityConfig(AuthFilterChain authFilterChain){
        this.authFilterChain=authFilterChain;

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf->csrf.disable()).authorizeHttpRequests(req->
              req.anyRequest().authenticated()).addFilterBefore(this.authFilterChain, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
