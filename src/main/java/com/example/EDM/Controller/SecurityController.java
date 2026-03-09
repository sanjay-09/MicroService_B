package com.example.EDM.Controller;


import com.example.EDM.Dtos.SecurityReqDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityController {


    @PostMapping
    @PreAuthorize("hasAuthority('RBAC_MANAGE')")
    public ResponseEntity<?> createSecurity(@RequestBody SecurityReqDto securityReqDto){


        return ResponseEntity.status(HttpStatus.OK).body("Security is created");

    }
}
