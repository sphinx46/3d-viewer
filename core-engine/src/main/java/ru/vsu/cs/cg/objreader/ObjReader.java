package ru.vsu.cs.cg.objreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exceptions.ObjReaderException;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.utils.MessageConstants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class ObjReader {

    private static final Logger LOG = LoggerFactory.getLogger(ObjReader.class);
    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";
    private static final String OBJ_MATERIAL_LIB_TOKEN = "mtllib";
    private static final String OBJ_USE_MATERIAL_TOKEN = "usemtl";

    public static Model read(String fileContent) {
        LOG.debug("Начало парсинга OBJ файла");

        Model result = new Model();
        String currentMaterialName = null;

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
                case OBJ_MATERIAL_LIB_TOKEN:
                    if (!wordsInLine.isEmpty()) {
                        result.setMaterialName(wordsInLine.get(0));
                        LOG.trace("Установлено имя файла материалов: {}", wordsInLine.get(0));
                    }
                    break;
                case OBJ_USE_MATERIAL_TOKEN:
                    if (!wordsInLine.isEmpty()) {
                        currentMaterialName = wordsInLine.get(0);
                        result.setMaterialName(currentMaterialName);
                        LOG.trace("Установлено имя материала: {}", currentMaterialName);
                    }
                    break;
                default:
                    LOG.trace("Неизвестный токен '{}' в строке {}", token, lineInd);
            }
        }

        scanner.close();

        LOG.info("OBJ файл успешно прочитан: вершин={}, текстур={}, нормалей={}, полигонов={}, материал={}",
            result.getVertices().size(),
            result.getTextureVertices().size(),
            result.getNormals().size(),
            result.getPolygons().size(),
            result.getMaterialName());

        return result;
    }

    public static Model readWithMaterial(String fileContent, String filePath) {
        LOG.debug("Начало парсинга OBJ файла с материалом: {}", filePath);

        Model model = read(fileContent);

        if (model.getMaterialName() != null) {
            try {
                loadMaterialFromMtl(filePath, model);
                LOG.info("Материал '{}' загружен для модели", model.getMaterialName());
            } catch (Exception e) {
                LOG.warn("Не удалось загрузить материал '{}': {}", model.getMaterialName(), e.getMessage());
            }
        }

        return model;
    }

    private static void loadMaterialFromMtl(String objFilePath, Model model) throws IOException {
        Path objPath = Paths.get(objFilePath);
        String mtlFileName = getMtlFileName(objFilePath, model.getMaterialName());

        if (mtlFileName == null || !Files.exists(Paths.get(mtlFileName))) {
            LOG.debug("MTL файл не найден: {}", mtlFileName);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(mtlFileName))) {
            String line;
            String currentMaterialName = null;
            float[] color = {0.8f, 0.8f, 0.8f};
            String texturePath = null;
            Float shininess = null;
            Float transparency = null;
            Float reflectivity = null;
            boolean useLighting = false;
            boolean useTexture = false;
            boolean drawPolygonalGrid = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("newmtl ")) {
                    if (currentMaterialName != null && currentMaterialName.equals(model.getMaterialName())) {
                        applyMaterialToModel(model, color, texturePath, shininess, transparency, reflectivity,
                            useLighting, useTexture, drawPolygonalGrid);
                        return;
                    }
                    currentMaterialName = line.substring(7).trim();
                    color = new float[]{0.8f, 0.8f, 0.8f};
                    texturePath = null;
                    shininess = null;
                    transparency = null;
                    reflectivity = null;
                    useLighting = false;
                    useTexture = false;
                    drawPolygonalGrid = false;
                } else if (currentMaterialName != null && currentMaterialName.equals(model.getMaterialName())) {
                    if (line.startsWith("Kd ")) {
                        String[] parts = line.substring(3).trim().split("\\s+");
                        if (parts.length >= 3) {
                            color[0] = Float.parseFloat(parts[0]);
                            color[1] = Float.parseFloat(parts[1]);
                            color[2] = Float.parseFloat(parts[2]);
                        }
                    } else if (line.startsWith("map_Kd ")) {
                        String textureFileName = line.substring(7).trim();
                        Path mtlDir = Paths.get(mtlFileName).getParent();

                        if (textureFileName != null && !textureFileName.isEmpty()) {
                            if (textureFileName.startsWith("#")) {
                                LOG.warn("Текстура закомментирована: {}", textureFileName);
                                continue;
                            }

                            Path texturePathObj = mtlDir.resolve(textureFileName);

                            if (Files.exists(texturePathObj)) {
                                texturePath = texturePathObj.toString();
                                useTexture = true;
                                LOG.debug("Текстура найдена: {}", texturePath);
                            } else {
                                LOG.warn("Файл текстуры не найден: {}", texturePathObj);
                                useTexture = false;
                            }
                        }
                    } else if (line.startsWith("Ns ")) {
                        shininess = Float.parseFloat(line.substring(3).trim()) / 1000.0f;
                        useLighting = shininess > 0;
                    } else if (line.startsWith("d ") || line.startsWith("Tr ")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length > 1) {
                            transparency = 1.0f - Float.parseFloat(parts[1]);
                        }
                    } else if (line.startsWith("Ks ")) {
                        String[] parts = line.substring(3).trim().split("\\s+");
                        if (parts.length >= 1) {
                            reflectivity = Float.parseFloat(parts[0]);
                        }
                    } else if (line.startsWith("# use_lighting ")) {
                        useLighting = Boolean.parseBoolean(line.substring(15).trim());
                    } else if (line.startsWith("# use_texture ")) {
                        useTexture = Boolean.parseBoolean(line.substring(14).trim());
                    } else if (line.startsWith("# draw_polygonal_grid ")) {
                        drawPolygonalGrid = Boolean.parseBoolean(line.substring(22).trim());
                    }
                }
            }

            if (currentMaterialName != null && currentMaterialName.equals(model.getMaterialName())) {
                applyMaterialToModel(model, color, texturePath, shininess, transparency, reflectivity,
                    useLighting, useTexture, drawPolygonalGrid);
            }
        }
    }

    private static String getMtlFileName(String objFilePath, String materialName) {
        Path objPath = Paths.get(objFilePath);
        String baseName = getFileNameWithoutExtension(objFilePath);

        Path possibleMtlPath = objPath.getParent().resolve(baseName + ".mtl");
        if (Files.exists(possibleMtlPath)) {
            return possibleMtlPath.toString();
        }

        if (materialName != null && materialName.toLowerCase().endsWith(".mtl")) {
            Path materialPath = objPath.getParent().resolve(materialName);
            if (Files.exists(materialPath)) {
                return materialPath.toString();
            }
        }

        return null;
    }

    private static String getFileNameWithoutExtension(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private static void applyMaterialToModel(Model model, float[] color, String texturePath,
                                             Float shininess, Float transparency, Float reflectivity,
                                             boolean useLighting, boolean useTexture, boolean drawPolygonalGrid) {
        model.setMaterialColor(color);
        model.setTexturePath(texturePath);
        model.setMaterialShininess(shininess);
        model.setMaterialTransparency(transparency);
        model.setMaterialReflectivity(reflectivity);
        model.setUseLighting(useLighting);
        model.setUseTexture(useTexture);
        model.setDrawPolygonalGrid(drawPolygonalGrid);

        LOG.info("Применен материал: цвет=[{},{},{}], текстура={}, блеск={}, прозрачность={}, отражение={}, освещение={}, текстура={}, сетка={}",
            color[0], color[1], color[2], texturePath, shininess, transparency, reflectivity,
            useLighting, useTexture, drawPolygonalGrid);
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
