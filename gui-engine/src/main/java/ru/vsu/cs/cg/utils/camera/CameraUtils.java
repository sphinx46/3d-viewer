package ru.vsu.cs.cg.utils.camera;

import java.util.Set;

public final class CameraUtils {
    private static final String DEFAULT_CAMERA_PREFIX = "Camera_";

    private CameraUtils() {
    }

    public static String generateUniqueCameraId(Set<String> existingIds) {
        int i = 1;
        while (true) {
            String potentialId = DEFAULT_CAMERA_PREFIX + i;
            if (!existingIds.contains(potentialId)) {
                return potentialId;
            }
            i++;
        }
    }
}
