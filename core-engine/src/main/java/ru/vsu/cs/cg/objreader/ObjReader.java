package ru.vsu.cs.cg.objreader;

import ru.vsu.cs.cg.exceptions.ObjReaderException;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.utils.MessageConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class ObjReader {

    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    /**
     * Читает содержимое файла OBJ и создает модель.
     *
     * @param fileContent содержимое файла OBJ
     * @return созданная модель
     */
    public static Model read(String fileContent) {
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
            }
        }

        scanner.close();
        return result;
    }

    /**
     * Парсит координаты вершины из списка слов.
     *
     * @param wordsInLineWithoutToken список слов без токена
     * @param lineInd                 номер строки для сообщений об ошибках
     * @return вектор с координатами вершины
     * @throws ObjReaderException если возникает ошибка парсинга
     */
    private static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken,
                                        int lineInd) {
        try {
            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException(MessageConstants.FLOAT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException(MessageConstants.TOO_FEW_VERTEX_ARGUMENTS_MESSAGE, lineInd);
        }
    }

    /**
     * Парсит текстурные координаты из списка слов.
     *
     * @param wordsInLineWithoutToken список слов без токена
     * @param lineInd                 номер строки для сообщений об ошибках
     * @return вектор с текстурными координатами
     * @throws ObjReaderException если возникает ошибка парсинга
     */
    private static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken,
                                               int lineInd) {
        try {
            return new Vector2f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException(MessageConstants.FLOAT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException(MessageConstants.TOO_FEW_VERTEX_ARGUMENTS_MESSAGE, lineInd);
        }
    }

    /**
     * Парсит нормали из списка слов.
     *
     * @param wordsInLineWithoutToken список слов без токена
     * @param lineInd                 номер строки для сообщений об ошибках
     * @return вектор с нормалью
     * @throws ObjReaderException если возникает ошибка парсинга
     */
    private static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        try {
            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException(MessageConstants.FLOAT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException(MessageConstants.TOO_FEW_NORMAL_ARGUMENTS_MESSAGE, lineInd);
        }
    }

    /**
     * Парсит полигон из списка слов.
     *
     * @param wordsInLineWithoutToken список слов без токена
     * @param lineInd                 номер строки для сообщений об ошибках
     * @return полигон с индексами вершин, текстурных координат и нормалей
     * @throws ObjReaderException если возникает ошибка парсинга
     */
    private static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        ArrayList<Integer> vertexIndices = new ArrayList<>();
        ArrayList<Integer> textureVertexIndices = new ArrayList<>();
        ArrayList<Integer> normalIndices = new ArrayList<>();

        for (String word : wordsInLineWithoutToken) {
            parseFaceWord(word, vertexIndices, textureVertexIndices, normalIndices, lineInd);
        }

        if (vertexIndices.size() < 3) {
            throw new ObjReaderException(MessageConstants.POLYGON_TOO_FEW_VERTICES_MESSAGE, lineInd);
        }

        Polygon result = new Polygon();
        result.setVertexIndices(vertexIndices);
        result.setTextureVertexIndices(textureVertexIndices);
        result.setNormalIndices(normalIndices);
        return result;
    }

    /**
     * Парсит одно слово в описании полигона.
     *
     * @param wordInLine           слово для парсинга
     * @param vertexIndices        список индексов вершин
     * @param textureVertexIndices список индексов текстурных координат
     * @param normalIndices        список индексов нормалей
     * @param lineInd              номер строки для сообщений об ошибках
     * @throws ObjReaderException если возникает ошибка парсинга
     */
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
                    vertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                    break;
                case 2:
                    vertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                    textureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
                    break;
                case 3:
                    vertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                    normalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
                    if (!wordIndices[1].isEmpty()) {
                        textureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
                    }
                    break;
                default:
                    throw new ObjReaderException(MessageConstants.INVALID_ELEMENT_SIZE_MESSAGE, lineInd);
            }

        } catch (NumberFormatException e) {
            throw new ObjReaderException(MessageConstants.INT_PARSE_ERROR_MESSAGE, lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException(MessageConstants.TOO_FEW_ARGUMENTS_MESSAGE, lineInd);
        }
    }
}
