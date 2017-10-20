package сom.viktor.yurlov.util;

import java.lang.reflect.Field;


public class BeanUtil {
    public static void copySameProperties(Object src, Object dst) {
        Class<?> srcClass = src.getClass();
        Class<?> dstClass = dst.getClass();

        for (Field srcField : srcClass.getDeclaredFields()) {
            srcField.setAccessible(true);

            try {
                Field dstField = dstClass.getDeclaredField(srcField.getName());
                if (dstField.getType().equals(srcField.getType())) {
                    dstField.setAccessible(true);
                    dstField.set(dst, srcField.get(src));
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) { }
        }
    }

    public static void copyNotNullProperties(Object src, Object dst) {
        Class<?> srcClass = src.getClass();
        Class<?> dstClass = dst.getClass();

        for (Field srcField : srcClass.getDeclaredFields()) {
            srcField.setAccessible(true);

            try {
                Field dstField = dstClass.getDeclaredField(srcField.getName());
                if (dstField.getType().equals(srcField.getType()) && srcField.get(src) != null) {
                    dstField.setAccessible(true);
                    dstField.set(dst, srcField.get(src));
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) { }
        }
    }
}
