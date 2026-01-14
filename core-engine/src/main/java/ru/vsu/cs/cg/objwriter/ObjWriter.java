package ru.vsu.cs.cg.objwriter;

import ru.vsu.cs.cg.exceptions.ObjWriterException;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.utils.MessageConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public final class ObjWriter {

    public static void write(String fileName, Model model) {
        write(fileName, model, null, null, null, null, null, null);
    }

    public static void write(String fileName, Model model, String materialName,
                             String texturePath, float[] color, Float shininess,
                             Float transparency, Float reflectivity) {
        File objFile = new File(fileName);

        try {
            if (objFile.createNewFile()) {
                System.out.println(MessageConstants.FILE_CREATED_MESSAGE + objFile.getName());
            } else {
                System.out.println(MessageConstants.FILE_ALREADY_EXISTS_MESSAGE);
            }
        } catch (IOException e) {
            throw new ObjWriterException(MessageConstants.FILE_WRITE_ERROR_MESSAGE + e.getMessage());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            if (materialName != null || color != null || texturePath != null ||
                shininess != null || transparency != null || reflectivity != null) {

                String mtlFileName = getMtlFileName(fileName);
                boolean useLighting = model.isUseLighting();
                boolean useTexture = model.isUseTexture() || texturePath != null;
                boolean drawPolygonalGrid = model.isDrawPolygonalGrid();

                writeMaterialFile(mtlFileName, materialName, texturePath, color,
                    shininess, transparency, reflectivity,
                    useLighting, useTexture, drawPolygonalGrid);
                writer.write("mtllib " + Paths.get(mtlFileName).getFileName() + "\n");
                if (materialName != null) {
                    writer.write("usemtl " + materialName + "\n");
                }
            }

            writeVertices(writer, model.getVertices());
            writeTextureVertices(writer, model.getTextureVertices());
            writeNormals(writer, model.getNormals());
            writePolygons(writer, model.getPolygons());
        } catch (IOException e) {
            throw new ObjWriterException(MessageConstants.FILE_WRITE_ERROR_MESSAGE + e.getMessage());
        }
    }

    private static void writeMaterialFile(String mtlFileName, String materialName,
                                          String texturePath, float[] color,
                                          Float shininess, Float transparency,
                                          Float reflectivity,
                                          boolean useLighting, boolean useTexture,
                                          boolean drawPolygonalGrid) throws IOException {
        File mtlFile = new File(mtlFileName);

        try (BufferedWriter mtlWriter = new BufferedWriter(new FileWriter(mtlFile))) {
            String actualMaterialName = materialName != null ? materialName : "default_material";
            mtlWriter.write("newmtl " + actualMaterialName + "\n");

            if (color != null && color.length >= 3) {
                mtlWriter.write(String.format(Locale.US, "Kd %.6f %.6f %.6f\n", color[0], color[1], color[2]));
            } else {
                mtlWriter.write("Kd 0.800000 0.800000 0.800000\n");
            }

            if (texturePath != null) {
                Path texturePathObj = Paths.get(texturePath);
                mtlWriter.write("map_Kd " + texturePathObj.getFileName() + "\n");
            }

            if (shininess != null) {
                mtlWriter.write(String.format(Locale.US, "Ns %.6f\n", shininess * 1000));
            } else {
                mtlWriter.write("Ns 500.000000\n");
            }

            if (transparency != null) {
                mtlWriter.write(String.format(Locale.US, "d %.6f\n", 1.0 - transparency));
            } else {
                mtlWriter.write("d 1.000000\n");
            }

            if (reflectivity != null) {
                mtlWriter.write(String.format(Locale.US, "Ks %.6f %.6f %.6f\n",
                    reflectivity, reflectivity, reflectivity));
            } else {
                mtlWriter.write("Ks 0.200000 0.200000 0.200000\n");
            }

            mtlWriter.write("# use_lighting " + useLighting + "\n");
            mtlWriter.write("# use_texture " + useTexture + "\n");
            mtlWriter.write("# draw_polygonal_grid " + drawPolygonalGrid + "\n");

            mtlWriter.write("illum 2\n");
        }
    }

    private static String getMtlFileName(String objFileName) {
        Path path = Paths.get(objFileName);
        String baseName = path.getFileName().toString();
        if (baseName.toLowerCase().endsWith(".obj")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        return path.getParent().resolve(baseName + ".mtl").toString();
    }

    private static void writeVertices(BufferedWriter writer, List<Vector3f> vertices)
        throws IOException {
        DecimalFormat decimalFormat = createDecimalFormat();

        for (Vector3f vertex : vertices) {
            writer.write("v " + decimalFormat.format(vertex.getX()) + " " +
                decimalFormat.format(vertex.getY()) + " " + decimalFormat.format(vertex.getZ()));
            writer.newLine();
        }
    }

    private static void writeTextureVertices(BufferedWriter writer, List<Vector2f> textureVertices)
        throws IOException {
        DecimalFormat decimalFormat = createDecimalFormat();

        for (Vector2f textureVertex : textureVertices) {
            writer.write("vt " + decimalFormat.format(textureVertex.getX()) + " "
                + decimalFormat.format(textureVertex.getY()));
            writer.newLine();
        }
    }

    private static void writeNormals(BufferedWriter writer, List<Vector3f> normals)
        throws IOException {
        DecimalFormat decimalFormat = createDecimalFormat();

        for (Vector3f normal : normals) {
            writer.write("vn " + decimalFormat.format(normal.getX()) + " "
                + decimalFormat.format(normal.getY()) + " " + decimalFormat.format(normal.getZ()));
            writer.newLine();
        }
    }

    private static void writePolygons(BufferedWriter writer, List<Polygon> polygons)
        throws IOException {
        for (Polygon polygon : polygons) {
            writer.write("f ");
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
            List<Integer> normalIndices = polygon.getNormalIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                if (!textureVertexIndices.isEmpty()) {
                    writer.write(vertexIndices.get(i) + 1 + "/"
                        + (textureVertexIndices.get(i) + 1));
                } else {
                    writer.write(String.valueOf(vertexIndices.get(i) + 1));
                }
                if (!normalIndices.isEmpty()) {
                    if (textureVertexIndices.isEmpty()) {
                        writer.write("/");
                    }
                    writer.write("/" + (normalIndices.get(i) + 1));
                }
                writer.write(" ");
            }
            writer.newLine();
        }
    }



    private static DecimalFormat createDecimalFormat() {
        DecimalFormatSymbols customSymbols = new DecimalFormatSymbols(Locale.US);
        customSymbols.setDecimalSeparator('.');
        return new DecimalFormat("0.######", customSymbols);
    }
}
