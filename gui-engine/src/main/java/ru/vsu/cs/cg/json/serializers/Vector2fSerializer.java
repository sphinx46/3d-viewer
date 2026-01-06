package ru.vsu.cs.cg.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.vsu.cs.cg.math.Vector2f;

import java.io.IOException;

public class Vector2fSerializer extends JsonSerializer<Vector2f> {
    @Override
    public void serialize(Vector2f vector, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("x", vector.getX());
        jsonGenerator.writeNumberField("y", vector.getY());
        jsonGenerator.writeEndObject();
    }
}
