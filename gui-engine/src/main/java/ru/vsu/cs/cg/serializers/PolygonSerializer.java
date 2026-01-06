package ru.vsu.cs.cg.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.vsu.cs.cg.model.Polygon;

import java.io.IOException;

public class PolygonSerializer extends JsonSerializer<Polygon> {
    @Override
    public void serialize(Polygon polygon, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeArrayFieldStart("vertexIndices");
        for (Integer index : polygon.getVertexIndices()) {
            jsonGenerator.writeNumber(index);
        }
        jsonGenerator.writeEndArray();

        if (!polygon.getTextureVertexIndices().isEmpty()) {
            jsonGenerator.writeArrayFieldStart("textureVertexIndices");
            for (Integer index : polygon.getTextureVertexIndices()) {
                jsonGenerator.writeNumber(index);
            }
            jsonGenerator.writeEndArray();
        }

        if (!polygon.getNormalIndices().isEmpty()) {
            jsonGenerator.writeArrayFieldStart("normalIndices");
            for (Integer index : polygon.getNormalIndices()) {
                jsonGenerator.writeNumber(index);
            }
            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeEndObject();
    }
}
