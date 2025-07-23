package com.example.backend.excecption;

import com.example.backend.excecption.api.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class InvalidRequestDataException extends RuntimeException{
    private String field;
    private String message;

    private static final String INVALID_REQUEST_DATA_MESSAGE = Status.BAD_REQUEST.getMessage();

    public InvalidRequestDataException(String field) {
        this(INVALID_REQUEST_DATA_MESSAGE, field);
    }
}
