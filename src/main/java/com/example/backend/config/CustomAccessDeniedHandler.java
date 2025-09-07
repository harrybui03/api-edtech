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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        ApiError apiError = ApiError.builder()
                .message(accessDeniedException.getMessage())
                .issue(Status.FORBIDDEN.getIssue())
                .httpStatus(Status.FORBIDDEN.getStatus())
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(Status.FORBIDDEN.getStatus().value());
        OutputStream responseStream = response.getOutputStream();
        objectMapper.writeValue(responseStream, apiError);
        responseStream.flush();
    }
}
