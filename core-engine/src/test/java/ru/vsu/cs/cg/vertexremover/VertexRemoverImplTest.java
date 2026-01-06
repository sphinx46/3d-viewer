package ru.vsu.cs.cg.vertexremover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.cg.exception.VertexRemoverException;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.objreader.ObjReader;
import ru.vsu.cs.cg.vertexremover.dto.VertexRemovalResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class VertexRemoverImplTest {

    private VertexRemover vertexRemover;
    private final static String BASE_TEST_RESOURCE_PATH = "src/test/resources";

    @BeforeEach
    void setUp() {
        vertexRemover = new VertexRemoverImpl();
    }

    @Test
    public void testRemoveAllVerticesWithClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);
        List<Integer> check = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, true);

        assertEquals(0, model.getVertices().size());
        assertEquals(8, result.getRemovedVerticesCount());
        assertEquals(6, result.getRemovedPolygonsCount());
        assertEquals(0, model.getPolygons().size());
    }

    @Test
    public void testRemoveAllVerticesWithoutClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);
        List<Integer> check = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, false);

        assertEquals(0, model.getVertices().size());
        assertEquals(8, result.getRemovedVerticesCount());
        assertEquals(6, result.getRemovedPolygonsCount());
        assertEquals(0, model.getPolygons().size());
    }

    @Test
    public void testRemoveSingleVertexWithClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = List.of(0);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, true);

        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(initialPolygonCount - 3, model.getPolygons().size());
        assertEquals(1, result.getRemovedVerticesCount());
        assertEquals(3, result.getRemovedPolygonsCount());
    }

    @Test
    public void testRemoveSingleVertexWithoutClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = List.of(0);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, false);

        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(initialPolygonCount - 3, model.getPolygons().size());
        assertEquals(1, result.getRemovedVerticesCount());
        assertEquals(3, result.getRemovedPolygonsCount());
    }

    @Test
    public void testPlaneCornerVertexWithClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/plane.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = List.of(0);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, true);

        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(initialPolygonCount - 1, model.getPolygons().size());
        assertEquals(1, result.getRemovedVerticesCount());
        assertEquals(1, result.getRemovedPolygonsCount());
    }

    @Test
    public void testPlaneCornerVertexWithoutClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/plane.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = List.of(0);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, false);

        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(initialPolygonCount - 1, model.getPolygons().size());
        assertEquals(1, result.getRemovedVerticesCount());
        assertEquals(1, result.getRemovedPolygonsCount());
    }

    @Test
    public void testPlaneInnerVertexWithClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/plane.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = List.of(12);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, true);

        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(1, result.getRemovedVerticesCount());
        assertEquals(4, result.getRemovedPolygonsCount());
        assertEquals(initialPolygonCount - 4, model.getPolygons().size());
    }

    @Test
    public void testPlaneInnerVertexWithoutClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/plane.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = List.of(12);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, false);

        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(1, result.getRemovedVerticesCount());
        assertEquals(4, result.getRemovedPolygonsCount());
        assertEquals(initialPolygonCount - 4, model.getPolygons().size());
    }

    @Test
    public void testCubeWithAdditionalVertexClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/cubeWithAdditionalV.obj"));
        Model model = ObjReader.read(fileContent);

        assertEquals(9, model.getVertices().size());
        assertEquals(7, model.getPolygons().size());

        List<Integer> check = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, true);

        assertEquals(0, model.getVertices().size());
        assertEquals(0, model.getPolygons().size());
        assertEquals(9, result.getRemovedVerticesCount());
        assertEquals(7, result.getRemovedPolygonsCount());
    }

    @Test
    public void testCubeWithAdditionalVertexNoClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/cubeWithAdditionalV.obj"));
        Model model = ObjReader.read(fileContent);

        assertEquals(9, model.getVertices().size());
        assertEquals(7, model.getPolygons().size());

        List<Integer> check = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, false);

        assertEquals(1, model.getVertices().size());
        assertEquals(0, model.getPolygons().size());
        assertEquals(8, result.getRemovedVerticesCount());
        assertEquals(7, result.getRemovedPolygonsCount());
    }

    @Test
    public void testEmptyVertexSetWithClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        Set<Integer> emptySet = new HashSet<>();
        VertexRemovalResult result = vertexRemover.removeVertices(model, emptySet, true);

        assertEquals(8, model.getVertices().size());
        assertEquals(6, model.getPolygons().size());
        assertEquals(0, result.getRemovedVerticesCount());
        assertEquals(0, result.getRemovedPolygonsCount());
    }

    @Test
    public void testEmptyVertexSetWithoutClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        Set<Integer> emptySet = new HashSet<>();
        VertexRemovalResult result = vertexRemover.removeVertices(model, emptySet, false);

        assertEquals(8, model.getVertices().size());
        assertEquals(6, model.getPolygons().size());
        assertEquals(0, result.getRemovedVerticesCount());
        assertEquals(0, result.getRemovedPolygonsCount());
    }

    @Test
    public void testMultipleVerticesWithClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = Arrays.asList(0, 3, 4);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, true);

        assertTrue(model.getVertices().size() < initialVertexCount);
        assertTrue(model.getPolygons().size() < initialPolygonCount);
        assertTrue(result.getRemovedVerticesCount() > 0);
        assertTrue(result.getRemovedPolygonsCount() > 0);
    }

    @Test
    public void testMultipleVerticesWithoutClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();
        List<Integer> check = Arrays.asList(0, 3, 4);

        Set<Integer> vertexIndices = new HashSet<>(check);
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, false);

        assertTrue(model.getVertices().size() < initialVertexCount);
        assertTrue(model.getPolygons().size() < initialPolygonCount);
        assertTrue(result.getRemovedVerticesCount() > 0);
        assertTrue(result.getRemovedPolygonsCount() > 0);
    }

    @Test
    public void testValidateRemovalRequestValid() throws IOException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        Set<Integer> validIndices = new HashSet<>(Arrays.asList(0, 1, 2));

        assertDoesNotThrow(() -> vertexRemover.validateRemovalRequest(model, validIndices));
    }

    @Test
    public void testValidateRemovalRequestInvalid() throws IOException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        Set<Integer> invalidIndices = new HashSet<>(Arrays.asList(10, 11));

        assertThrows(VertexRemoverException.class,
                () -> vertexRemover.validateRemovalRequest(model, invalidIndices));
    }

    @Test
    public void testValidateRemovalRequestNullSet() throws IOException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/simpleCube.obj"));
        Model model = ObjReader.read(fileContent);

        assertThrows(VertexRemoverException.class,
                () -> vertexRemover.validateRemovalRequest(model, null));
    }

    @Test
    public void testPlaneWithTextureAndNormalsClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/cubeWithAdditionalV.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        Set<Integer> vertexIndices = new HashSet<>(Arrays.asList(0, 1, 2));
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, true);

        assertTrue(model.getVertices().size() < initialVertexCount);
        assertTrue(model.getPolygons().size() < initialPolygonCount);
        assertTrue(result.getRemovedVerticesCount() > 0);
        assertTrue(result.getRemovedPolygonsCount() > 0);
    }

    @Test
    public void testPlaneWithTextureAndNormalsNoClearUnused() throws IOException, VertexRemoverException {
        String fileContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH +
                "/cubeWithAdditionalV.obj"));
        Model model = ObjReader.read(fileContent);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        Set<Integer> vertexIndices = new HashSet<>(Arrays.asList(0, 1, 2));
        VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, false);

        assertTrue(model.getVertices().size() < initialVertexCount);
        assertTrue(model.getPolygons().size() < initialPolygonCount);
        assertTrue(result.getRemovedVerticesCount() > 0);
        assertTrue(result.getRemovedPolygonsCount() > 0);
    }
}
