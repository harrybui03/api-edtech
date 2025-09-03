package com.example.backend.controller;

import com.example.backend.dto.request.auth.LoginRequest;
import com.example.backend.dto.request.auth.RefreshTokenRequest;
import com.example.backend.dto.request.auth.SignupRequest;
import com.example.backend.dto.response.auth.JwtAuthResponse;
import com.example.backend.service.AuthService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("auth")
@Data
@AllArgsConstructor
public class AuthController {
    private AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) throws BadRequestException {
        authService.signUp(signupRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws BadRequestException {
        authService.logIn(loginRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam(name="token") String token) throws BadRequestException {
        JwtAuthResponse response = authService.verifyToken(token);
        return  ResponseEntity.ok().body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) throws BadRequestException {
        return ResponseEntity.ok().body(authService.refresh(request.getRefreshToken()));
    }

}
