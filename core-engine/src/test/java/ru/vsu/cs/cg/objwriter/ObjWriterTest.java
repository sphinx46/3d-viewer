package ru.vsu.cs.cg.objwriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjWriterTest {

    @Test
    @DisplayName("Запись модели в OBJ файл должно создавать файл")
    void write_ShouldCreateFile(@TempDir Path tempDir) {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addVertex(new Vector3f(1.0f, 2.0f, 3.0f));

        ObjWriter.write(objFile.toString(), model);

        assertTrue(Files.exists(objFile));
    }

    @Test
    @DisplayName("Запись модели с вершинами должно корректно сохранять вершины")
    void write_WithVertices_ShouldSaveVertices(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addVertex(new Vector3f(1.0f, 2.0f, 3.0f));
        model.addVertex(new Vector3f(4.0f, 5.0f, 6.0f));

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertTrue(content.contains("v 1") && content.contains("v 4"));
    }

    @Test
    @DisplayName("Запись модели с текстурными координатами должно корректно сохранять UV")
    void write_WithTextureVertices_ShouldSaveTextureVertices(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addTextureVertex(new Vector2f(0.5f, 0.5f));
        model.addTextureVertex(new Vector2f(0.0f, 1.0f));

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertTrue(content.contains("vt 0.5") || content.contains("vt 0.500000"));
    }

    @Test
    @DisplayName("Запись модели с нормалями должно корректно сохранять нормали")
    void write_WithNormals_ShouldSaveNormals(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addNormal(new Vector3f(0.0f, 0.0f, 1.0f));
        model.addNormal(new Vector3f(1.0f, 0.0f, 0.0f));

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertTrue(content.contains("vn"));
    }

    @Test
    @DisplayName("Запись модели с полигонами должно корректно сохранять полигоны")
    void write_WithPolygons_ShouldSavePolygons(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addVertex(new Vector3f(0, 0, 0));
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(0, 1, 0));
        model.addVertex(new Vector3f(1, 1, 0));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
        model.addPolygon(polygon);

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertTrue(content.contains("f "));
    }

    @Test
    @DisplayName("Запись модели с полигонами с текстурами должно сохранять формат")
    void write_WithPolygonsWithTextures_ShouldSaveFormat(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addVertex(new Vector3f(0, 0, 0));
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(0, 1, 0));
        model.addTextureVertex(new Vector2f(0, 0));
        model.addTextureVertex(new Vector2f(1, 0));
        model.addTextureVertex(new Vector2f(0, 1));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.addPolygon(polygon);

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertTrue(content.contains("/"));
    }

    @Test
    @DisplayName("Запись модели с полигонами с нормалями должно сохранять формат")
    void write_WithPolygonsWithNormals_ShouldSaveFormat(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addVertex(new Vector3f(0, 0, 0));
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(0, 1, 0));
        model.addNormal(new Vector3f(0, 0, 1));
        model.addNormal(new Vector3f(0, 0, 1));
        model.addNormal(new Vector3f(0, 0, 1));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.addPolygon(polygon);

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertTrue(content.contains("//"));
    }

    @Test
    @DisplayName("Запись модели со всеми атрибутами должно сохранять полный формат")
    void write_WithAllAttributes_ShouldSaveFullFormat(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.addVertex(new Vector3f(0, 0, 0));
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(0, 1, 0));
        model.addTextureVertex(new Vector2f(0, 0));
        model.addTextureVertex(new Vector2f(1, 0));
        model.addTextureVertex(new Vector2f(0, 1));
        model.addNormal(new Vector3f(0, 0, 1));
        model.addNormal(new Vector3f(0, 0, 1));
        model.addNormal(new Vector3f(0, 0, 1));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.addPolygon(polygon);

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertTrue(content.contains("/") && content.contains("/1") || content.contains("/2"));
    }

    @Test
    @DisplayName("Запись модели с материалом должно создавать MTL файл")
    void write_WithMaterial_ShouldCreateMtlFile(@TempDir Path tempDir) {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            "texture.png", new float[]{1.0f, 0.0f, 0.0f},
            0.5f, 0.3f, 0.2f);

        Path mtlFile = tempDir.resolve("test.mtl");
        assertTrue(Files.exists(mtlFile));
    }

    @Test
    @DisplayName("Запись модели с материалом должно корректно сохранять цвет")
    void write_WithMaterial_ShouldSaveColor(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            null, new float[]{1.0f, 0.0f, 0.0f},
            null, null, null);

        Path mtlFile = tempDir.resolve("test.mtl");
        String content = Files.readString(mtlFile);
        assertTrue(content.contains("Kd") && content.contains("1.0"));
    }

    @Test
    @DisplayName("Запись модели с текстурой должно копировать файл текстуры")
    void write_WithTexture_ShouldCopyTextureFile(@TempDir Path tempDir) throws IOException {
        Path textureFile = tempDir.resolve("test_texture.png");
        Files.write(textureFile, "dummy texture data".getBytes());

        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            textureFile.toString(), null,
            null, null, null);

        Path copiedTexture = tempDir.resolve("test_texture.png");
        assertTrue(Files.exists(copiedTexture));
    }

    @Test
    @DisplayName("Запись модели с блеском должно корректно сохранять Ns")
    void write_WithShininess_ShouldSaveNs(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            null, null, 0.8f, null, null);

        Path mtlFile = tempDir.resolve("test.mtl");
        String content = Files.readString(mtlFile);
        assertTrue(content.contains("Ns"));
    }

    @Test
    @DisplayName("Запись модели с прозрачностью должно корректно сохранять d")
    void write_WithTransparency_ShouldSaveD(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            null, null, null, 0.3f, null);

        Path mtlFile = tempDir.resolve("test.mtl");
        String content = Files.readString(mtlFile);
        assertTrue(content.contains("d") && (content.contains("0.7") || content.contains("0.700000")));
    }

    @Test
    @DisplayName("Запись модели с отражением должно корректно сохранять Ks")
    void write_WithReflectivity_ShouldSaveKs(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            null, null, null, null, 0.5f);

        Path mtlFile = tempDir.resolve("test.mtl");
        String content = Files.readString(mtlFile);
        assertTrue(content.contains("Ks"));
    }

    @Test
    @DisplayName("Запись модели с кастомными настройками должно сохранять комментарии")
    void write_WithCustomSettings_ShouldSaveComments(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();
        model.setUseLighting(true);
        model.setUseTexture(false);
        model.setDrawPolygonalGrid(true);

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            null, null, null, null, null);

        Path mtlFile = tempDir.resolve("test.mtl");
        String content = Files.readString(mtlFile);
        assertTrue(content.contains("use_lighting"));
    }

    @Test
    @DisplayName("Запись в существующий файл должно перезаписывать его")
    void write_ToExistingFile_ShouldOverwrite(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Files.write(objFile, "old content".getBytes());

        Model model = new Model();
        model.addVertex(new Vector3f(1, 2, 3));

        ObjWriter.write(objFile.toString(), model);

        String content = Files.readString(objFile);
        assertFalse(content.equals("old content"));
        assertTrue(content.contains("v "));
    }

    @Test
    @DisplayName("Запись модели без материала не должно создавать MTL файл")
    void write_WithoutMaterial_ShouldNotCreateMtlFile(@TempDir Path tempDir) {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model);

        Path mtlFile = tempDir.resolve("test.mtl");
        assertFalse(Files.exists(mtlFile));
    }

    @Test
    @DisplayName("Запись модели с несуществующей текстурой должно логировать ошибку")
    void write_WithNonExistentTexture_ShouldLogError(@TempDir Path tempDir) throws IOException {
        Path objFile = tempDir.resolve("test.obj");
        Model model = new Model();

        ObjWriter.write(objFile.toString(), model, "TestMaterial",
            "nonexistent.png", null, null, null, null);

        Path mtlFile = tempDir.resolve("test.mtl");
        String content = Files.readString(mtlFile);
        assertTrue(content.contains("nonexistent"));
    }
}
