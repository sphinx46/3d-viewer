package ru.vsu.cs.cg.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.vsu.cs.cg.math.Vector3f;

import java.io.IOException;

public class Vector3fDeserializer extends JsonDeserializer<Vector3f> {
    @Override
    public Vector3f deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        float x = node.get("x").floatValue();
        float y = node.get("y").floatValue();
        float z = node.get("z").floatValue();
        return new Vector3f(x, y, z);
    }
}
