
package ru.vsu.cs.cg.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorSerializer extends JsonSerializer<Color> {
    @Override
    public void serialize(Color color, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("red", color.getRed());
        jsonGenerator.writeNumberField("green", color.getGreen());
        jsonGenerator.writeNumberField("blue", color.getBlue());
        jsonGenerator.writeNumberField("opacity", color.getOpacity());
        jsonGenerator.writeEndObject();
    }
}
