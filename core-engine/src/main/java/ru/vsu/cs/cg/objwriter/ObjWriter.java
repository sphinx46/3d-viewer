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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public final class ObjWriter {

    /**
     * Записывает модель в файл OBJ.
     *
     * @param fileName имя файла для записи
     * @param model модель для записи
     * @throws ObjWriterException если возникает ошибка при записи файла
     */
    public static void write(String fileName, Model model) {
        File file = new File(fileName);

        try {
            if (file.createNewFile()) {
                System.out.println(MessageConstants.FILE_CREATED_MESSAGE + file.getName());
            } else {
                System.out.println(MessageConstants.FILE_ALREADY_EXISTS_MESSAGE);
            }
        } catch (IOException e) {
            throw new ObjWriterException(MessageConstants.FILE_WRITE_ERROR_MESSAGE + e.getMessage());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writeVertices(writer, model.vertices);
            writeTextureVertices(writer, model.textureVertices);
            writeNormals(writer, model.normals);
            writePolygons(writer, model.polygons);
        } catch (IOException e) {
            throw new ObjWriterException(MessageConstants.FILE_WRITE_ERROR_MESSAGE + e.getMessage());
        }
    }

    /**
     * Записывает вершины в файл OBJ.
     *
     * @param writer BufferedWriter для записи
     * @param vertices список вершин
     * @throws IOException если возникает ошибка ввода-вывода
     */
    private static void writeVertices(BufferedWriter writer, List<Vector3f> vertices)
            throws IOException {
        DecimalFormat decimalFormat = createDecimalFormat();

        for (Vector3f vertex : vertices) {
            writer.write("v " + decimalFormat.format(vertex.getX()) + " " +
                    decimalFormat.format(vertex.getY()) + " " + decimalFormat.format(vertex.getZ()));
            writer.newLine();
        }
    }

    /**
     * Записывает текстурные координаты в файл OBJ.
     *
     * @param writer BufferedWriter для записи
     * @param textureVertices список текстурных координат
     * @throws IOException если возникает ошибка ввода-вывода
     */
    private static void writeTextureVertices(BufferedWriter writer, List<Vector2f> textureVertices)
            throws IOException {
        DecimalFormat decimalFormat = createDecimalFormat();

        for (Vector2f textureVertex : textureVertices) {
            writer.write("vt " + decimalFormat.format(textureVertex.getX()) + " "
                    + decimalFormat.format(textureVertex.getY()));
            writer.newLine();
        }
    }

    /**
     * Записывает нормали в файл OBJ.
     *
     * @param writer BufferedWriter для записи
     * @param normals список нормалей
     * @throws IOException если возникает ошибка ввода-вывода
     */
    private static void writeNormals(BufferedWriter writer, List<Vector3f> normals)
            throws IOException {
        DecimalFormat decimalFormat = createDecimalFormat();

        for (Vector3f normal : normals) {
            writer.write("vn " + decimalFormat.format(normal.getX()) + " "
                    + decimalFormat.format(normal.getY()) + " " + decimalFormat.format(normal.getZ()));
            writer.newLine();
        }
    }

    /**
     * Записывает полигоны в файл OBJ.
     *
     * @param writer BufferedWriter для записи
     * @param polygons список полигонов
     * @throws IOException если возникает ошибка ввода-вывода
     */
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

    /**
     * Создает DecimalFormat с настройками для форматирования чисел.
     *
     * @return DecimalFormat с точкой в качестве разделителя
     */
    private static DecimalFormat createDecimalFormat() {
        DecimalFormatSymbols customSymbols = new DecimalFormatSymbols(Locale.US);
        customSymbols.setDecimalSeparator('.');
        return new DecimalFormat("0.######", customSymbols);
    }
}