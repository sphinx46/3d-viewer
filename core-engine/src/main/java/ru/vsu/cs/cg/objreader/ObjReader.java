package ru.vsu.cs.cg.objreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exception.ObjReaderException;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.utils.MessageConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class ObjReader {

    private static final Logger LOG = LoggerFactory.getLogger(ObjReader.class);
    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    public static Model read(String fileContent) {
        LOG.debug("Начало парсинга OBJ файла");

        Model result = new Model();

        int lineInd = 0;
        Scanner scanner = new Scanner(fileContent);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
            if (wordsInLine.isEmpty()) {
                continue;
            }

            final String token = wordsInLine.get(0);
            wordsInLine.remove(0);

            ++lineInd;

            switch (token) {
                case OBJ_VERTEX_TOKEN:
                    result.addVertex(parseVertex(wordsInLine, lineInd));
                    break;
                case OBJ_TEXTURE_TOKEN:
                    result.addTextureVertex(parseTextureVertex(wordsInLine, lineInd));
                    break;
                case OBJ_NORMAL_TOKEN:
                    result.addNormal(parseNormal(wordsInLine, lineInd));
                    break;
                case OBJ_FACE_TOKEN:
                    result.addPolygon(parseFace(wordsInLine, lineInd));
                    break;
                default:
                    LOG.trace("Неизвестный токен '{}' в строке {}", token, lineInd);
            }
        }

        scanner.close();

        LOG.info("OBJ файл успешно прочитан: вершин={}, текстур={}, нормалей={}, полигонов={}",
            result.getVertices().size(),
            result.getTextureVertices().size(),
            result.getNormals().size(),
            result.getPolygons().size());

        return result;
    }

    private static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken,
                                        int lineInd) {
        try {
            float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
            float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
            float z = Float.parseFloat(wordsInLineWithoutToken.get(2));

            LOG.trace("Прочитана вершина: [{}, {}, {}] в строке {}", x, y, z, lineInd);
            return new Vector3f(x, y, z);

        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга float в строке {}: {}", lineInd, e.getMessage());
            throw new ObjReaderException(MessageConstants.FLOAT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            LOG.error("Слишком мало аргументов для вершины в строке {}", lineInd);
            throw new ObjReaderException(MessageConstants.TOO_FEW_VERTEX_ARGUMENTS_MESSAGE, lineInd);
        }
    }

    private static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken,
                                               int lineInd) {
        try {
            float u = Float.parseFloat(wordsInLineWithoutToken.get(0));
            float v = Float.parseFloat(wordsInLineWithoutToken.get(1));

            LOG.trace("Прочитаны текстурные координаты: [{}, {}] в строке {}", u, v, lineInd);
            return new Vector2f(u, v);

        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга float в строке {}: {}", lineInd, e.getMessage());
            throw new ObjReaderException(MessageConstants.FLOAT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            LOG.error("Слишком мало аргументов для текстурных координат в строке {}", lineInd);
            throw new ObjReaderException(MessageConstants.TOO_FEW_VERTEX_ARGUMENTS_MESSAGE, lineInd);
        }
    }

    private static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        try {
            float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
            float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
            float z = Float.parseFloat(wordsInLineWithoutToken.get(2));

            LOG.trace("Прочитана нормаль: [{}, {}, {}] в строке {}", x, y, z, lineInd);
            return new Vector3f(x, y, z);

        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга float в строке {}: {}", lineInd, e.getMessage());
            throw new ObjReaderException(MessageConstants.FLOAT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            LOG.error("Слишком мало аргументов для нормали в строке {}", lineInd);
            throw new ObjReaderException(MessageConstants.TOO_FEW_NORMAL_ARGUMENTS_MESSAGE, lineInd);
        }
    }

    private static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        LOG.trace("Парсинг полигона в строке {}", lineInd);

        ArrayList<Integer> vertexIndices = new ArrayList<>();
        ArrayList<Integer> textureVertexIndices = new ArrayList<>();
        ArrayList<Integer> normalIndices = new ArrayList<>();

        for (String word : wordsInLineWithoutToken) {
            parseFaceWord(word, vertexIndices, textureVertexIndices, normalIndices, lineInd);
        }

        if (vertexIndices.size() < 3) {
            LOG.error("Полигон содержит менее 3 вершин в строке {}", lineInd);
            throw new ObjReaderException(MessageConstants.POLYGON_TOO_FEW_VERTICES_MESSAGE, lineInd);
        }

        Polygon result = new Polygon();
        result.setVertexIndices(vertexIndices);
        result.setTextureVertexIndices(textureVertexIndices);
        result.setNormalIndices(normalIndices);

        LOG.trace("Создан полигон с {} вершинами в строке {}", vertexIndices.size(), lineInd);
        return result;
    }

    private static void parseFaceWord(
        String wordInLine,
        ArrayList<Integer> vertexIndices,
        ArrayList<Integer> textureVertexIndices,
        ArrayList<Integer> normalIndices,
        int lineInd) {
        try {
            String[] wordIndices = wordInLine.split("/");
            int length = wordIndices.length;

            switch (length) {
                case 1:
                    int vertexIndex = Integer.parseInt(wordIndices[0]) - 1;
                    vertexIndices.add(vertexIndex);
                    LOG.trace("Вершина с индексом {} в строке {}", vertexIndex, lineInd);
                    break;
                case 2:
                    vertexIndex = Integer.parseInt(wordIndices[0]) - 1;
                    int textureIndex = Integer.parseInt(wordIndices[1]) - 1;
                    vertexIndices.add(vertexIndex);
                    textureVertexIndices.add(textureIndex);
                    LOG.trace("Вершина {} с текстурой {} в строке {}", vertexIndex, textureIndex, lineInd);
                    break;
                case 3:
                    vertexIndex = Integer.parseInt(wordIndices[0]) - 1;
                    int normalIndex = Integer.parseInt(wordIndices[2]) - 1;
                    vertexIndices.add(vertexIndex);
                    normalIndices.add(normalIndex);
                    if (!wordIndices[1].isEmpty()) {
                        textureIndex = Integer.parseInt(wordIndices[1]) - 1;
                        textureVertexIndices.add(textureIndex);
                        LOG.trace("Вершина {} с текстурой {} и нормалью {} в строке {}",
                            vertexIndex, textureIndex, normalIndex, lineInd);
                    } else {
                        LOG.trace("Вершина {} с нормалью {} в строке {}", vertexIndex, normalIndex, lineInd);
                    }
                    break;
                default:
                    LOG.error("Неверный размер элемента в строке {}: '{}'", lineInd, wordInLine);
                    throw new ObjReaderException(MessageConstants.INVALID_ELEMENT_SIZE_MESSAGE, lineInd);
            }

        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга int в строке {}: '{}'", lineInd, wordInLine);
            throw new ObjReaderException(MessageConstants.INT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            LOG.error("Слишком мало аргументов в строке {}: '{}'", lineInd, wordInLine);
            throw new ObjReaderException(MessageConstants.TOO_FEW_ARGUMENTS_MESSAGE, lineInd);
        }
    }
}
