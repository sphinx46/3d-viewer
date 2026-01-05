package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.objreader.ObjReader;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


public class NormalsCalculatorTest {
    private ArrayList<Vector3f> trueNormals;
    private Model model;

    @BeforeEach
    void init() throws IOException {
        Path path = Paths.get("src/test/resources/CubeWithVertexNormals.obj");
        File file = path.toFile();
        String fileContent = Files.readString(file.toPath());
        model = ObjReader.read(fileContent);
    }

    //При тестировании надо увеличить EPSILON в классе Vector3f иначе всегда будет false
    @Test
    void normalTest(){

        ArrayList<Vector3f> trueNormal = new ArrayList<>(model.getNormals());
        NormalCalculator.computeVertexNormals(model.getVertices(), model.getPolygons());

        for(int i = 0; i < model.getNormals().size(); i++){
            System.out.println(trueNormal.get(i));
            System.out.println(model.getNormals().get(i));
            System.out.println("===========");
            assertEquals(model.getNormals().get(i), trueNormal.get(i));
        }
    }

}