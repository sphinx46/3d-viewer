package ru.vsu.cs.cg.model;

import ru.vsu.cs.cg.exception.ValidateVertexException;
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

    public Polygon(ArrayList<Integer> vertexIndices , ArrayList<Integer> textureVertexIndices, ArrayList<Integer> normalIndices) {
        this.vertexIndices = new ArrayList<>(vertexIndices);
        this.textureVertexIndices = new ArrayList<>(textureVertexIndices);
        this.normalIndices = new ArrayList<>(normalIndices);
    }

    /**
     * Выполняется триангуляция веером
     * @return список полигонов
     */
    public List<Polygon> triangulate() {
        List<Polygon> triangles = new ArrayList<>();

        boolean hasTextureVertex = !textureVertexIndices.isEmpty();
        boolean hasNormal = !normalIndices.isEmpty();

        for (int i = 1; i < vertexIndices.size() - 1; i++) {
            Polygon polygon = new Polygon();

            ArrayList<Integer> trV = new ArrayList<>();
            ArrayList<Integer> trT = new ArrayList<>();
            ArrayList<Integer> trN = new ArrayList<>();

            trV.add(vertexIndices.get(0));
            trV.add(vertexIndices.get(i));
            trV.add(vertexIndices.get(i + 1));
            polygon.setVertexIndices(trV);

            if (hasTextureVertex) {
                trT.add(textureVertexIndices.get(0));
                trT.add(textureVertexIndices.get(i));
                trT.add(textureVertexIndices.get(i + 1));
                polygon.setTextureVertexIndices(trT);
            }

            if (hasNormal) {
                trN.add(normalIndices.get(0));
                trN.add(normalIndices.get(i));
                trN.add(normalIndices.get(i + 1));
                polygon.setNormalIndices(trN);
            }

            triangles.add(polygon);
        }

        return triangles;
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
