package ru.vsu.cs.cg.exception;

public class ObjReaderException extends RuntimeException {
    public ObjReaderException(String errorMessage, int lineInd) {
        super("Ошибка парсинга OBJ файла на строке: " + lineInd + ". " + errorMessage);
    }
}
