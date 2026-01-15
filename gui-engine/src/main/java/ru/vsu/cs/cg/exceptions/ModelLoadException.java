package ru.vsu.cs.cg.exceptions;

public class ModelLoadException extends RuntimeException {

    public ModelLoadException(String message) {
        super(message);
    }

    public ModelLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
