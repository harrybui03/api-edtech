package com.example.backend.excecption.api;

import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.InternalServerError;
import com.example.backend.excecption.InvalidRequestDataException;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ApiErrorBuilder {
    public static ResponseEntity<ApiError> buildApiErrorResponse(Exception e , Status status) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .errorId(status.getErrorId())
                        .message(status.getMessage())
                        .httpStatus(status.getStatus())
                        .details(buildApiErrorDetails(e))
                        .build(),
                status.getStatus());
    }

    private static List<ApiErrorDetail> buildApiErrorDetails(Exception exception) {
        if (exception instanceof DataNotFoundException){
            return buildDetails((DataNotFoundException) exception);
        } else if (exception instanceof InvalidRequestDataException){
            return buildDetails((InvalidRequestDataException) exception);
        }

        return buildDetails((InternalServerError) exception);
    }

    private static List<ApiErrorDetail> buildDetails(InternalServerError exception){
        return List.of(
                ApiErrorDetail.builder().field(exception.getField()).issue(exception.getMessage()).build()
        );
    }

    private static List<ApiErrorDetail> buildDetails(DataNotFoundException exception){
        return List.of(ApiErrorDetail.builder().field(exception.getField()).issue(exception.getMessage()).build());
    }

    private static List<ApiErrorDetail> buildDetails(InvalidRequestDataException exception){
        return List.of(ApiErrorDetail.builder().field(exception.getField()).issue(exception.getMessage()).build());
    }
}
