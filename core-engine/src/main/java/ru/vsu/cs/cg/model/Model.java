package ru.vsu.cs.cg.model;


import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;

import java.util.ArrayList;

public final class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();
}