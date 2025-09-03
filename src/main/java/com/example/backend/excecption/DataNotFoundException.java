package com.example.backend.excecption;

import com.example.backend.excecption.api.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DataNotFoundException extends EntityNotFoundException {
    private String field;
    private String message;

    private static final String NOT_FOUND_MESSAGE = Status.NOT_FOUND.getMessage();

    public DataNotFoundException(String field) {
        this(field, NOT_FOUND_MESSAGE);
    }
}
