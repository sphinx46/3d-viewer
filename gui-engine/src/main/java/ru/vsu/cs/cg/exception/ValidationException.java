package ru.vsu.cs.cg.exception;

import ru.vsu.cs.cg.exception.ApplicationException;

public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
