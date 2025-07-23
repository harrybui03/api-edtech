package com.example.backend.controller;

import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.InternalServerError;
import com.example.backend.excecption.InvalidRequestDataException;
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

    private void logException(Throwable exception){
        log.error(String.format("Message=\"%s\", exception=%s", exception.getMessage(), exception));
    }

}
