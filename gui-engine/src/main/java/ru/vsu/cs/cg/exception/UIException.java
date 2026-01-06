package ru.vsu.cs.cg.exception;

public class UIException extends ApplicationException {

    public UIException(String message) {
        super(message);
    }

    public UIException(String message, Throwable cause) {
        super(message, cause);
    }
}
