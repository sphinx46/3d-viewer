package ru.vsu.cs.cg.model;

import ru.vsu.cs.cg.exceptions.ValidateVertexException;
import ru.vsu.cs.cg.utils.MessageConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Polygon {
    private List<Integer> vertexIndices;
    private List<Integer> textureVertexIndices;
    private List<Integer> normalIndices;

    public Polygon() {
        vertexIndices = new ArrayList<>();
        textureVertexIndices = new ArrayList<>();
        normalIndices = new ArrayList<>();
    }

    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        if (vertexIndices == null || vertexIndices.size() < 3) {
            throw new ValidateVertexException(MessageConstants.TOO_FEW_VERTEX_ARGUMENTS_MESSAGE);
        }
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(List<Integer> textureVertexIndices) {
        this.textureVertexIndices = Objects.requireNonNullElseGet(textureVertexIndices, ArrayList::new);
    }

    public void setNormalIndices(List<Integer> normalIndices) {
        this.normalIndices = Objects.requireNonNullElseGet(normalIndices, ArrayList::new);
    }

    public List<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public List<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public List<Integer> getNormalIndices() {
        return normalIndices;
    }
}