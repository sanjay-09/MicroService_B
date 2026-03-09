package com.example.EDM.Dto;

import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private String username;
    private List<String> roles;
    private boolean valid;
}