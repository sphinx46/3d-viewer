package ru.vsu.cs.cg.utils.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
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
}
