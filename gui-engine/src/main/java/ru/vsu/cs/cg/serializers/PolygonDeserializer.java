package ru.vsu.cs.cg.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.vsu.cs.cg.model.Polygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PolygonDeserializer extends JsonDeserializer<Polygon> {
    @Override
    public Polygon deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        List<Integer> vertexIndices = parseIntegerList(node.get("vertexIndices"));
        List<Integer> textureVertexIndices = parseIntegerList(node.get("textureVertexIndices"));
        List<Integer> normalIndices = parseIntegerList(node.get("normalIndices"));

        Polygon polygon = new Polygon();
        for (Integer index : vertexIndices) {
            polygon.getVertexIndices().add(index);
        }
        for (Integer index : textureVertexIndices) {
            polygon.getTextureVertexIndices().add(index);
        }
        for (Integer index : normalIndices) {
            polygon.getNormalIndices().add(index);
        }

        return polygon;
    }

    private List<Integer> parseIntegerList(JsonNode node) {
        List<Integer> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode element : node) {
                list.add(element.intValue());
            }
        }
        return list;
    }
}
