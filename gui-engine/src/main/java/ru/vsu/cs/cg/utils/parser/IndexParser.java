package ru.vsu.cs.cg.utils.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IndexParser {
    private static final Logger LOG = LoggerFactory.getLogger(IndexParser.class);

    private IndexParser() {
    }

    public static Set<Integer> parseIndices(String input) {
        Set<Integer> indices = new HashSet<>();

        if (input == null || input.trim().isEmpty()) {
            return indices;
        }

        try {
            String[] parts = input.split(",");

            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.contains("-")) {
                    parseRange(trimmed, indices);
                } else {
                    parseSingleIndex(trimmed, indices);
                }
            }

            LOG.debug("Успешно распарсены индексы: {}", indices);
        } catch (Exception e) {
            LOG.error("Ошибка парсинга индексов '{}': {}", input, e.getMessage());
            indices.clear();
        }

        return indices;
    }

    private static void parseRange(String range, Set<Integer> indices) {
        String[] bounds = range.split("-");
        if (bounds.length != 2) {
            throw new IllegalArgumentException("Некорректный формат диапазона: " + range);
        }

        int start = Integer.parseInt(bounds[0].trim());
        int end = Integer.parseInt(bounds[1].trim());

        if (start > end) {
            throw new IllegalArgumentException("Начало диапазона больше конца: " + range);
        }

        for (int i = start; i <= end; i++) {
            indices.add(i);
        }
    }

    private static void parseSingleIndex(String indexStr, Set<Integer> indices) {
        int index = Integer.parseInt(indexStr.trim());
        indices.add(index);
    }

    public static boolean validateIndices(Set<Integer> indices, int maxIndex) {
        if (indices == null) {
            return false;
        }

        for (Integer index : indices) {
            if (index < 0 || index >= maxIndex) {
                LOG.warn("Индекс {} выходит за пределы допустимого диапазона 0-{}", index, maxIndex - 1);
                return false;
            }
        }

        return true;
    }

    public static Set<Integer> parseAndValidateIndices(String input, int maxIndex) {
        Set<Integer> indices = parseIndices(input);
        if (!validateIndices(indices, maxIndex)) {
            throw new IllegalArgumentException("Один или несколько индексов выходят за пределы допустимого диапазона");
        }
        return indices;
    }

    public static String formatIndices(List<Integer> indices) {
        if (indices == null || indices.isEmpty()) {
            return "";
        }

        List<Integer> sorted = indices.stream()
            .distinct()
            .sorted()
            .toList();

        StringBuilder result = new StringBuilder();
        Integer rangeStart = null;
        Integer rangeEnd = null;

        for (int i = 0; i < sorted.size(); i++) {
            int current = sorted.get(i);

            if (rangeStart == null) {
                rangeStart = current;
                rangeEnd = current;
            } else if (current == rangeEnd + 1) {
                rangeEnd = current;
            } else {
                if (rangeStart.equals(rangeEnd)) {
                    result.append(rangeStart);
                } else {
                    result.append(rangeStart).append("-").append(rangeEnd);
                }
                result.append(", ");
                rangeStart = current;
                rangeEnd = current;
            }
        }

        if (rangeStart != null) {
            if (rangeStart.equals(rangeEnd)) {
                result.append(rangeStart);
            } else {
                result.append(rangeStart).append("-").append(rangeEnd);
            }
        }

        return result.toString();
    }
}
