package com.example.backend.controller;

import com.example.backend.excecption.*;
import com.example.backend.excecption.api.ApiError;
import com.example.backend.excecption.api.ApiErrorBuilder;
import com.example.backend.excecption.api.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler({DataNotFoundException.class})
    public ResponseEntity<ApiError> handleDataNotFoundException(DataNotFoundException exception){
        logException(exception);
        return ApiErrorBuilder.buildApiErrorResponse(exception, Status.NOT_FOUND);
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException exception){
        logException(exception);
        return ApiErrorBuilder.buildApiErrorResponse(exception, Status.NOT_FOUND);
    }

    @ExceptionHandler({InvalidRequestDataException.class})
    public ResponseEntity<ApiError> handleInvalidRequestDataException(InvalidRequestDataException exception){
        logException(exception);
        return ApiErrorBuilder.buildApiErrorResponse(exception, Status.BAD_REQUEST);
    }

    @ExceptionHandler({InternalServerError.class})
    public ResponseEntity<ApiError> handleInternalServerErrorException(InternalServerError exception){
        logException(exception);
        return ApiErrorBuilder.buildApiErrorResponse(exception, Status.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException exception) {
        logException(exception);
        return ApiErrorBuilder.buildApiErrorResponse(exception, Status.FORBIDDEN);
    }

    @ExceptionHandler({UnauthorizedException.class})
    public  ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException exception) {
        logException(exception);
        return ApiErrorBuilder.buildApiErrorResponse(exception, Status.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception exception) {
        logException(exception);
        return ApiErrorBuilder.buildApiErrorResponse(exception, Status.INTERNAL_SERVER_ERROR);
    }

    private void logException(Throwable exception){
        log.error("Message=\"{}\", exception={}", exception.getMessage(), exception);
    }

}
