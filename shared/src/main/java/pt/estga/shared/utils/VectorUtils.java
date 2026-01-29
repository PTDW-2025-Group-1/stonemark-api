package pt.estga.shared.utils;

import java.util.List;

public class VectorUtils {

    private VectorUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a List of Float to a float array.
     *
     * @param list The list of floats to convert.
     * @return A float array containing the elements of the list, or null if the list is null.
     */
    public static float[] toFloatArray(List<Float> list) {
        if (list == null) {
            return null;
        }
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
