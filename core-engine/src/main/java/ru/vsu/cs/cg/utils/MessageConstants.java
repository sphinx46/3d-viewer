package ru.vsu.cs.cg.utils;

/**
 * Утилитарный класс для константных сообщений.
 */
public final class MessageConstants {

    // Общие сообщения об ошибках парсинга
    public static final String FLOAT_PARSE_ERROR_MESSAGE = "Ошибка! Не удалось разобрать значение float.";
    public static final String INT_PARSE_ERROR_MESSAGE = "Ошибка! Не удалось разобрать значение int.";
    public static final String TOO_FEW_ARGUMENTS_MESSAGE = "Слишком мало аргументов.";
    public static final String INVALID_ELEMENT_SIZE_MESSAGE = "Неверный размер элемента.";

    // Сообщения для вершин
    public static final String TOO_FEW_VERTEX_ARGUMENTS_MESSAGE = "Слишком мало аргументов для вершины.";
    public static final String VERTICES_CANNOT_DELETE_FAILURE = "Невозможно удалить указанные вершины";

    // Сообщения для нормалей
    public static final String TOO_FEW_NORMAL_ARGUMENTS_MESSAGE = "Слишком мало аргументов для нормали.";

    // Сообщения для полигонов
    public static final String POLYGON_TOO_FEW_VERTICES_MESSAGE = "Полигон должен содержать хотя бы 3 вершины.";

    // Сообщения для записи файлов
    public static final String FILE_WRITE_ERROR_MESSAGE = "Ошибка при записи файла: ";
    public static final String FILE_CREATED_MESSAGE = "Файл успешно создан: ";
    public static final String FILE_ALREADY_EXISTS_MESSAGE = "Файл уже существует.";
}