package com.example.backend.excecption;

import com.example.backend.excecption.api.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InternalServerError  extends RuntimeException{
    private String field;
    private String message;

    private static final String INTERNAL_SERVER_ERROR = Status.INTERNAL_SERVER_ERROR.getMessage();

    public InternalServerError(String field) {
        this(field , INTERNAL_SERVER_ERROR);
    }
}
