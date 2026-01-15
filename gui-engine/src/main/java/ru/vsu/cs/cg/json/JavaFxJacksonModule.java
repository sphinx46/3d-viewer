package ru.vsu.cs.cg.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.scene.paint.Color;
import ru.vsu.cs.cg.json.deserializers.ColorDeserializer;
import ru.vsu.cs.cg.json.deserializers.PolygonDeserializer;
import ru.vsu.cs.cg.json.deserializers.Vector2fDeserializer;
import ru.vsu.cs.cg.json.deserializers.Vector3fDeserializer;
import ru.vsu.cs.cg.json.serializers.ColorSerializer;
import ru.vsu.cs.cg.json.serializers.PolygonSerializer;
import ru.vsu.cs.cg.json.serializers.Vector2fSerializer;
import ru.vsu.cs.cg.json.serializers.Vector3fSerializer;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Polygon;

public class JavaFxJacksonModule extends SimpleModule {
    public JavaFxJacksonModule() {
        addSerializer(Vector2f.class, new Vector2fSerializer());
        addDeserializer(Vector2f.class, new Vector2fDeserializer());
        addSerializer(Vector3f.class, new Vector3fSerializer());
        addDeserializer(Vector3f.class, new Vector3fDeserializer());
        addSerializer(Polygon.class, new PolygonSerializer());
        addDeserializer(Polygon.class, new PolygonDeserializer());
        addSerializer(Color.class, new ColorSerializer());
        addDeserializer(Color.class, new ColorDeserializer());
    }
}
