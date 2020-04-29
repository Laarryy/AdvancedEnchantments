package me.egg82.ae.utils;

public class ReflectUtil {
    private ReflectUtil() { }

    public static boolean hasMethod(String methodName, Class<?> clazz) {
        if (methodName == null || clazz == null) {
            return false;
        }

        try {
            clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException | SecurityException ignored) {
            return false;
        }

        return true;
    }

    public static boolean hasMethod(String methodName, Object obj) {
        if (methodName == null || obj == null) {
            return false;
        }

        try {
            obj.getClass().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException | SecurityException ignored) {
            return false;
        }

        return true;
    }
}
