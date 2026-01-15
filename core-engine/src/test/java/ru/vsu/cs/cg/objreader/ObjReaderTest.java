package ru.vsu.cs.cg.objreader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.vsu.cs.cg.exceptions.ObjReaderException;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ObjReaderTest {

    @Test
    @DisplayName("Чтение OBJ с вершинами должно корректно парсить вершины")
    void read_WithVertices_ShouldParseVertices() {
        String content = "v 1.0 2.0 3.0\nv 4.0 5.0 6.0";
        Model model = ObjReader.read(content);

        assertEquals(2, model.getVertices().size());
        assertEquals(new Vector3f(1.0f, 2.0f, 3.0f), model.getVertices().get(0));
        assertEquals(new Vector3f(4.0f, 5.0f, 6.0f), model.getVertices().get(1));
    }

    @Test
    @DisplayName("Чтение OBJ с текстурными координатами должно корректно парсить UV")
    void read_WithTextureVertices_ShouldParseTextureVertices() {
        String content = "vt 0.5 0.5\nvt 0.0 1.0";
        Model model = ObjReader.read(content);

        assertEquals(2, model.getTextureVertices().size());
        Vector2f expected1 = new Vector2f(0.5f, 0.5f);
        Vector2f expected2 = new Vector2f(0.0f, 1.0f);

        assertTrue(expected1.equals(model.getTextureVertices().get(0)) ||
            expected1.toString().equals(model.getTextureVertices().get(0).toString()));
        assertTrue(expected2.equals(model.getTextureVertices().get(1)) ||
            expected2.toString().equals(model.getTextureVertices().get(1).toString()));
    }

    @Test
    @DisplayName("Чтение OBJ с нормалями должно корректно парсить нормали")
    void read_WithNormals_ShouldParseNormals() {
        String content = "vn 0.0 0.0 1.0\nvn 1.0 0.0 0.0";
        Model model = ObjReader.read(content);

        assertEquals(2, model.getNormals().size());
        assertEquals(new Vector3f(0.0f, 0.0f, 1.0f), model.getNormals().get(0));
        assertEquals(new Vector3f(1.0f, 0.0f, 0.0f), model.getNormals().get(1));
    }

    @Test
    @DisplayName("Чтение OBJ с полигонами должно корректно парсить полигоны")
    void read_WithFaces_ShouldParseFaces() {
        String content = "v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3";
        Model model = ObjReader.read(content);

        assertEquals(1, model.getPolygons().size());
        Polygon polygon = model.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), polygon.getVertexIndices());
    }

    @Test
    @DisplayName("Чтение OBJ с полигонами с текстурами должно корректно парсить индексы")
    void read_WithFacesWithTextures_ShouldParseIndices() {
        String content = "v 0 0 0\nv 1 0 0\nv 0 1 0\n" +
            "vt 0 0\nvt 1 0\nvt 0 1\n" +
            "f 1/1 2/2 3/3";
        Model model = ObjReader.read(content);

        Polygon polygon = model.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), polygon.getVertexIndices());
        assertEquals(Arrays.asList(0, 1, 2), polygon.getTextureVertexIndices());
    }

    @Test
    @DisplayName("Чтение OBJ с полигонами с нормалями должно корректно парсить все индексы")
    void read_WithFacesWithNormals_ShouldParseAllIndices() {
        String content = "v 0 0 0\nv 1 0 0\nv 0 1 0\n" +
            "vn 0 0 1\nvn 0 0 1\nvn 0 0 1\n" +
            "f 1//1 2//2 3//3";
        Model model = ObjReader.read(content);

        Polygon polygon = model.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), polygon.getVertexIndices());
        assertEquals(Arrays.asList(0, 1, 2), polygon.getNormalIndices());
        assertTrue(polygon.getTextureVertexIndices().isEmpty());
    }

    @Test
    @DisplayName("Чтение OBJ с полигонами со всеми атрибутами должно корректно парсить")
    void read_WithFacesWithAllAttributes_ShouldParseCorrectly() {
        String content = "v 0 0 0\nv 1 0 0\nv 0 1 0\n" +
            "vt 0 0\nvt 1 0\nvt 0 1\n" +
            "vn 0 0 1\nvn 0 0 1\nvn 0 0 1\n" +
            "f 1/1/1 2/2/2 3/3/3";
        Model model = ObjReader.read(content);

        Polygon polygon = model.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), polygon.getVertexIndices());
        assertEquals(Arrays.asList(0, 1, 2), polygon.getTextureVertexIndices());
        assertEquals(Arrays.asList(0, 1, 2), polygon.getNormalIndices());
    }

    @Test
    @DisplayName("Чтение OBJ с материалом должно устанавливать имя материала")
    void read_WithMaterial_ShouldSetMaterialName() {
        String content = "mtllib test.mtl\nusemtl MyMaterial";
        Model model = ObjReader.read(content);

        assertEquals("MyMaterial", model.getMaterialName());
    }

    @Test
    @DisplayName("Парсинг вершины с недостаточным количеством аргументов должно выбрасывать исключение")
    void parseVertex_WithTooFewArguments_ShouldThrowException() {
        ObjReaderException exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read("v 1.0 2.0");
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("слишком мало") ||
            exception.getMessage().contains("вершин") ||
            exception.getMessage().contains("аргумент"));
    }

    @Test
    @DisplayName("Парсинг текстурных координат с недостаточным количеством аргументов должно выбрасывать исключение")
    void parseTextureVertex_WithTooFewArguments_ShouldThrowException() {
        ObjReaderException exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read("vt 0.5");
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("слишком мало") ||
            exception.getMessage().contains("текстур") ||
            exception.getMessage().contains("аргумент"));
    }

    @Test
    @DisplayName("Парсинг нормали с недостаточным количеством аргументов должно выбрасывать исключение")
    void parseNormal_WithTooFewArguments_ShouldThrowException() {
        ObjReaderException exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read("vn 0.0 1.0");
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("слишком мало") ||
            exception.getMessage().contains("нормал") ||
            exception.getMessage().contains("аргумент"));
    }

    @Test
    @DisplayName("Парсинг полигона с менее чем 3 вершинами должно выбрасывать исключение")
    void parseFace_WithLessThan3Vertices_ShouldThrowException() {
        ObjReaderException exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read("v 0 0 0\nv 1 0 0\nf 1 2");
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("менее 3") ||
            exception.getMessage().contains("вершин") ||
            exception.getMessage().contains("полигон"));
    }

    @Test
    @DisplayName("Парсинг элемента лица с некорректным форматом должно выбрасывать исключение")
    void parseFaceWord_WithInvalidFormat_ShouldThrowException() {
        ObjReaderException exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read("v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1/2/3/4 2 3");
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("элемент") ||
            exception.getMessage().contains("размер") ||
            exception.getMessage().contains("формат"));
    }

    @Test
    @DisplayName("Чтение OBJ с пустыми строками и комментариями должно игнорировать их")
    void read_WithEmptyLinesAndComments_ShouldIgnoreThem() {
        String content = "# Comment\n\nv 1 2 3\n\n# Another comment\nv 4 5 6";
        Model model = ObjReader.read(content);

        assertEquals(2, model.getVertices().size());
    }

    @Test
    @DisplayName("Чтение OBJ с неизвестными токенами должно логировать и продолжать парсинг")
    void read_WithUnknownTokens_ShouldContinueParsing() {
        String content = "v 1 2 3\nunknown_token\nv 4 5 6";
        Model model = ObjReader.read(content);

        assertEquals(2, model.getVertices().size());
    }

    @Test
    @DisplayName("Чтение OBJ с материалами из MTL файла должно загружать материал")
    void readWithMaterial_WithMtlFile_ShouldLoadMaterial(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Path mtlFile = tempDir.resolve("test.mtl");

        String objContent = "mtllib test.mtl\nusemtl TestMaterial\nv 0 0 0";
        String mtlContent = "newmtl TestMaterial\nKd 1.0 0.0 0.0\nNs 500.0";

        Files.write(objFile, objContent.getBytes());
        Files.write(mtlFile, mtlContent.getBytes());

        Model model = ObjReader.readWithMaterial(objContent, objFile.toString());

        assertEquals("TestMaterial", model.getMaterialName());
        assertNotNull(model.getMaterialColor());
    }

    @Test
    @DisplayName("Чтение OBJ с материалами без MTL файла не должно падать")
    void readWithMaterial_WithoutMtlFile_ShouldNotFail(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");

        String objContent = "mtllib nonexistent.mtl\nusemtl TestMaterial\nv 0 0 0";
        Files.write(objFile, objContent.getBytes());

        Model model = ObjReader.readWithMaterial(objContent, objFile.toString());

        assertNotNull(model);
        assertEquals("TestMaterial", model.getMaterialName());
    }
}
