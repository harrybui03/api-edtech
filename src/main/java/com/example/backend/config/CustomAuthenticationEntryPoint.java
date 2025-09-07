package com.example.backend.config;

import com.example.backend.excecption.api.ApiError;
import com.example.backend.excecption.api.ApiErrorBuilder;
import com.example.backend.excecption.api.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        ApiError apiError = ApiError.builder()
                .message(authException.getMessage())
                .issue(Status.UNAUTHORIZED.getIssue())
                .httpStatus(Status.UNAUTHORIZED.getStatus())
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(Status.UNAUTHORIZED.getStatus().value());
        OutputStream responseStream = response.getOutputStream();
        objectMapper.writeValue(responseStream, apiError);
        responseStream.flush();
    }
}