package ru.vsu.cs.cg.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.objreader.ObjReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class IndexUtilsTest {

    private final static String BASE_TEST_RESOURCE_PATH = "src/test/resources";
    private Model simpleCubeModel;
    private Model cubeWithAdditionalVModel;

    @BeforeEach
    void setUp() throws IOException {
        String simpleCubeContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH + "/simpleCube.obj"));
        simpleCubeModel = ObjReader.read(simpleCubeContent);

        String cubeWithAdditionalVContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH + "/cubeWithAdditionalV.obj"));
        cubeWithAdditionalVModel = ObjReader.read(cubeWithAdditionalVContent);
    }

    @Test
    @DisplayName("Получение всех используемых индексов вершин из модели")
    public void testGetAllUsedVertexIndices() {
        Set<Integer> vertexIndices = IndexUtils.getAllUsedVertexIndices(simpleCubeModel);

        assertNotNull(vertexIndices);
        assertEquals(8, vertexIndices.size());
        for (int i = 0; i < 8; i++) {
            assertTrue(vertexIndices.contains(i));
        }
    }

    @Test
    @DisplayName("Получение всех используемых текстурных индексов из модели")
    public void testGetAllUsedTextureVertexIndices() {
        Set<Integer> textureIndices = IndexUtils.getAllUsedTextureVertexIndices(cubeWithAdditionalVModel);

        assertNotNull(textureIndices);
        assertFalse(textureIndices.isEmpty());
    }

    @Test
    @DisplayName("Получение всех используемых нормалей из модели")
    public void testGetAllUsedNormalIndices() {
        Set<Integer> normalIndices = IndexUtils.getAllUsedNormalIndices(cubeWithAdditionalVModel);

        assertNotNull(normalIndices);
        assertFalse(normalIndices.isEmpty());
    }

    @Test
    @DisplayName("Получение всех используемых индексов с пользовательским экстрактором")
    public void testGetAllUsedIndicesWithCustomExtractor() {
        Set<Integer> vertexIndices = IndexUtils.getAllUsedIndices(simpleCubeModel, IndexUtils.VERTEX_EXTRACTOR);

        assertNotNull(vertexIndices);
        assertEquals(8, vertexIndices.size());
        for (int i = 0; i < 8; i++) {
            assertTrue(vertexIndices.contains(i));
        }
    }

    @Test
    @DisplayName("Создание маппинга индексов с исключением удаленных индексов")
    public void testCreateIndexMappingExcluding() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(1, 3, 5));

        Map<Integer, Integer> mapping = IndexUtils.createIndexMappingExcluding(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertEquals(3, mapping.size());
        assertEquals(0, mapping.get(0));
        assertEquals(1, mapping.get(2));
        assertEquals(2, mapping.get(4));
        assertNull(mapping.get(1));
        assertNull(mapping.get(3));
        assertNull(mapping.get(5));
    }

    @Test
    @DisplayName("Создание маппинга индексов с пустым набором используемых индексов")
    public void testCreateIndexMappingExcludingEmptyUsedIndices() {
        Set<Integer> usedIndices = new HashSet<>();
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(1, 2, 3));

        Map<Integer, Integer> mapping = IndexUtils.createIndexMappingExcluding(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertTrue(mapping.isEmpty());
    }

    @Test
    @DisplayName("Создание маппинга индексов с null набором удаленных индексов")
    public void testCreateIndexMappingExcludingNullRemovedIndices() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));

        Map<Integer, Integer> mapping = IndexUtils.createIndexMappingExcluding(usedIndices, null);

        assertNotNull(mapping);
        assertEquals(5, mapping.size());
        assertEquals(0, mapping.get(0));
        assertEquals(1, mapping.get(1));
        assertEquals(2, mapping.get(2));
        assertEquals(3, mapping.get(3));
        assertEquals(4, mapping.get(4));
    }

    @Test
    @DisplayName("Создание маппинга индексов с удалением всех индексов")
    public void testCreateIndexMappingExcludingAllRemoved() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));

        Map<Integer, Integer> mapping = IndexUtils.createIndexMappingExcluding(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertTrue(mapping.isEmpty());
    }

    @Test
    @DisplayName("Создание полного маппинга индексов с исключением удаленных")
    public void testCreateFullIndexMapping() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 2, 4, 6, 8));
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(2, 6));

        Map<Integer, Integer> mapping = IndexUtils.createFullIndexMapping(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertEquals(3, mapping.size());
        assertEquals(0, mapping.get(0));
        assertEquals(1, mapping.get(4));
        assertEquals(2, mapping.get(8));
        assertNull(mapping.get(2));
        assertNull(mapping.get(6));
    }

    @Test
    @DisplayName("Создание полного маппинга индексов с непрерывными индексами")
    public void testCreateFullIndexMappingContinuousIndices() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(1, 3));

        Map<Integer, Integer> mapping = IndexUtils.createFullIndexMapping(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertEquals(3, mapping.size());
        assertEquals(0, mapping.get(0));
        assertEquals(1, mapping.get(2));
        assertEquals(2, mapping.get(4));
        assertNull(mapping.get(1));
        assertNull(mapping.get(3));
    }

    @Test
    @DisplayName("Создание полного маппинга индексов с пустым набором используемых индексов")
    public void testCreateFullIndexMappingEmptyUsedIndices() {
        Set<Integer> usedIndices = new HashSet<>();
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(1, 2, 3));

        Map<Integer, Integer> mapping = IndexUtils.createFullIndexMapping(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertTrue(mapping.isEmpty());
    }

    @Test
    @DisplayName("Создание полного маппинга индексов с null набором удаленных индексов")
    public void testCreateFullIndexMappingNullRemovedIndices() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 2, 4));

        Map<Integer, Integer> mapping = IndexUtils.createFullIndexMapping(usedIndices, null);

        assertNotNull(mapping);
        assertEquals(3, mapping.size());
        assertEquals(0, mapping.get(0));
        assertEquals(1, mapping.get(2));
        assertEquals(2, mapping.get(4));
    }

    @Test
    @DisplayName("Создание полного маппинга индексов с удалением всех индексов")
    public void testCreateFullIndexMappingAllRemoved() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 2, 4));
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(0, 2, 4));

        Map<Integer, Integer> mapping = IndexUtils.createFullIndexMapping(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertTrue(mapping.isEmpty());
    }

    @Test
    @DisplayName("Создание полного маппинга индексов с неиспользуемыми промежуточными индексами")
    public void testCreateFullIndexMappingWithUnusedIntermediate() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 5, 10));
        Set<Integer> removedIndices = new HashSet<>(List.of(5));

        Map<Integer, Integer> mapping = IndexUtils.createFullIndexMapping(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertEquals(2, mapping.size());
        assertEquals(0, mapping.get(0));
        assertEquals(1, mapping.get(10));
        assertNull(mapping.get(5));
    }

    @Test
    @DisplayName("Создание полного маппинга для одного индекса")
    public void testCreateFullIndexMappingSingleIndex() {
        Set<Integer> usedIndices = new HashSet<>(List.of(5));
        Set<Integer> removedIndices = new HashSet<>();

        Map<Integer, Integer> mapping = IndexUtils.createFullIndexMapping(usedIndices, removedIndices);

        assertNotNull(mapping);
        assertEquals(1, mapping.size());
        assertEquals(0, mapping.get(5));
    }

    @Test
    @DisplayName("Сравнение createIndexMappingExcluding и createFullIndexMapping")
    public void testCompareMappingMethods() {
        Set<Integer> usedIndices = new HashSet<>(Arrays.asList(0, 5, 10));
        Set<Integer> removedIndices = new HashSet<>(List.of(5));

        Map<Integer, Integer> mappingExcluding = IndexUtils.createIndexMappingExcluding(usedIndices, removedIndices);
        Map<Integer, Integer> mappingFull = IndexUtils.createFullIndexMapping(usedIndices, removedIndices);

        assertNotNull(mappingExcluding);
        assertNotNull(mappingFull);

        assertEquals(2, mappingExcluding.size());
        assertEquals(2, mappingFull.size());

        assertEquals(mappingExcluding.get(0), mappingFull.get(0));
        assertEquals(mappingExcluding.get(10), mappingFull.get(10));
    }

    @Test
    @DisplayName("Получение индексов из модели с полигонами, содержащими текстуры и нормали")
    public void testGetAllIndicesFromModelWithTexturesAndNormals() {
        Set<Integer> vertexIndices = IndexUtils.getAllUsedVertexIndices(cubeWithAdditionalVModel);
        Set<Integer> textureIndices = IndexUtils.getAllUsedTextureVertexIndices(cubeWithAdditionalVModel);
        Set<Integer> normalIndices = IndexUtils.getAllUsedNormalIndices(cubeWithAdditionalVModel);

        assertNotNull(vertexIndices);
        assertNotNull(textureIndices);
        assertNotNull(normalIndices);
        assertFalse(vertexIndices.isEmpty());
        assertFalse(textureIndices.isEmpty());
        assertFalse(normalIndices.isEmpty());
    }

    @Test
    @DisplayName("Создание маппинга для реальной модели")
    public void testCreateMappingForRealModel() {
        Set<Integer> usedVertexIndices = IndexUtils.getAllUsedVertexIndices(simpleCubeModel);
        Set<Integer> removedIndices = new HashSet<>(Arrays.asList(0, 1));

        Map<Integer, Integer> mapping = IndexUtils.createIndexMappingExcluding(usedVertexIndices, removedIndices);

        assertNotNull(mapping);
        assertEquals(6, mapping.size());

        for (int i = 2; i < 8; i++) {
            assertTrue(mapping.containsKey(i));
            assertTrue(mapping.get(i) >= 0);
        }

        assertFalse(mapping.containsKey(0));
        assertFalse(mapping.containsKey(1));
    }
}
