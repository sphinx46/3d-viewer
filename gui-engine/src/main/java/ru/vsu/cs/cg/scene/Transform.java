package ru.vsu.cs.cg.scene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transform {
    private static final Logger LOG = LoggerFactory.getLogger(Transform.class);

    private double positionX;
    private double positionY;
    private double positionZ;
    private double rotationX;
    private double rotationY;
    private double rotationZ;
    private double scaleX;
    private double scaleY;
    private double scaleZ;

    @JsonCreator
    public Transform(
        @JsonProperty("positionX") double positionX,
        @JsonProperty("positionY") double positionY,
        @JsonProperty("positionZ") double positionZ,
        @JsonProperty("rotationX") double rotationX,
        @JsonProperty("rotationY") double rotationY,
        @JsonProperty("rotationZ") double rotationZ,
        @JsonProperty("scaleX") double scaleX,
        @JsonProperty("scaleY") double scaleY,
        @JsonProperty("scaleZ") double scaleZ) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        LOG.debug("Создан Transform: pos({}, {}, {}), rot({}, {}, {}), scale({}, {}, {})",
            positionX, positionY, positionZ, rotationX, rotationY, rotationZ,
            scaleX, scaleY, scaleZ);
    }

    public Transform() {
        this(0, 0, 0, 0, 0, 0, 1, 1, 1);
    }

    public double getPositionX() { return positionX; }
    public void setPositionX(double positionX) { this.positionX = positionX; }

    public double getPositionY() { return positionY; }
    public void setPositionY(double positionY) { this.positionY = positionY; }

    public double getPositionZ() { return positionZ; }
    public void setPositionZ(double positionZ) { this.positionZ = positionZ; }

    public double getRotationX() { return rotationX; }
    public void setRotationX(double rotationX) { this.rotationX = rotationX; }

    public double getRotationY() { return rotationY; }
    public void setRotationY(double rotationY) { this.rotationY = rotationY; }

    public double getRotationZ() { return rotationZ; }
    public void setRotationZ(double rotationZ) { this.rotationZ = rotationZ; }

    public double getScaleX() { return scaleX; }
    public void setScaleX(double scaleX) { this.scaleX = scaleX; }

    public double getScaleY() { return scaleY; }
    public void setScaleY(double scaleY) { this.scaleY = scaleY; }

    public double getScaleZ() { return scaleZ; }
    public void setScaleZ(double scaleZ) { this.scaleZ = scaleZ; }

    public void reset() {
        positionX = positionY = positionZ = rotationX = rotationY = rotationZ = 0;
        scaleX = scaleY = scaleZ = 1;
        LOG.debug("Transform сброшен к значениям по умолчанию");
    }

    @Override
    public String toString() {
        return String.format("Transform[pos=(%.2f, %.2f, %.2f), rot=(%.2f, %.2f, %.2f), scale=(%.2f, %.2f, %.2f)]",
            positionX, positionY, positionZ, rotationX, rotationY, rotationZ,
            scaleX, scaleY, scaleZ);
    }
}
