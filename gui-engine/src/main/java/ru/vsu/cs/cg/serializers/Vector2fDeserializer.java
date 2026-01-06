package ru.vsu.cs.cg.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.vsu.cs.cg.math.Vector2f;

import java.io.IOException;

public class Vector2fDeserializer extends JsonDeserializer<Vector2f> {
    @Override
    public Vector2f deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        float x = node.get("x").floatValue();
        float y = node.get("y").floatValue();
        return new Vector2f(x, y);
    }
}
