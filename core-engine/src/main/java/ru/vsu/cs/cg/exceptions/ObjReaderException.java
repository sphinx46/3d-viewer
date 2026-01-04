package ru.vsu.cs.cg.exceptions;

public class ObjReaderException extends RuntimeException {
    public ObjReaderException(String errorMessage, int lineInd) {
        super("Ошибка парсинга OBJ файла на строке: " + lineInd + ". " + errorMessage);
    }
}