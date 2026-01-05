package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.objreader.ObjReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NormalsCalculatorTest {
    private Model model;

    private static final float TEST_EPSILON = 1e-4f;

    @BeforeEach
    void init() throws IOException {
        Path path = Paths.get("src/test/resources/CubeWithVertexNormals.obj");
        File file = path.toFile();
        String fileContent = Files.readString(file.toPath());
        model = ObjReader.read(fileContent);
    }

    @Test
    void normalTest() {
        ArrayList<Vector3f> trueNormal = (ArrayList<Vector3f>) model.normals.clone();

        NormalCalculator.calculateVerticesNormals(model);

        assertEquals(trueNormal.size(), model.normals.size(), "Количество нормалей не совпадает");

        for (int i = 0; i < model.normals.size(); i++) {
            Vector3f expected = trueNormal.get(i);
            Vector3f actual = model.normals.get(i);

            assertEquals(expected.getX(), actual.getX(), TEST_EPSILON, "Не совпадает X у вершины " + i);
            assertEquals(expected.getY(), actual.getY(), TEST_EPSILON, "Не совпадает Y у вершины " + i);
            assertEquals(expected.getZ(), actual.getZ(), TEST_EPSILON, "Не совпадает Z у вершины " + i);
        }
    }

}
