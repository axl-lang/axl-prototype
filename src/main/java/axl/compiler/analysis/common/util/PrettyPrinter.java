package axl.compiler.analysis.common.util;

import axl.compiler.analysis.common.data.SourceLocation;
import axl.compiler.analysis.lexer.data.Token;
import lombok.SneakyThrows;

import java.lang.reflect.*;
import java.util.*;

public class PrettyPrinter {

    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    public static void print(Object obj) {
        if (obj instanceof Type) {
            print(obj.toString(), 0);
            return;
        }

        print(obj, 0);
    }

    private static void print(Object obj, int indent) {
        if (obj == null || isEmpty(obj)) return;

        Class<?> clazz = obj.getClass();

        if (isSingleLine(obj)) {
            println(color(toLiteral(obj), BLUE), indent);
            return;
        }

        if (obj instanceof Collection<?> collection) {
            println("[", indent);
            for (Object item : collection) {
                print(item, indent + 4);
            }

            println("]", indent);
            return;
        }

        if (clazz.isArray()) {
            int length = Array.getLength(obj);
            println("[", indent);
            for (int i = 0; i < length; i++) {
                print(Array.get(obj, i), indent + 4);
            }
            println("]", indent);
            return;
        }

        if (obj instanceof Map<?, ?> map) {
            println("{", indent);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (isEmpty(value)) continue;
                String key = String.valueOf(entry.getKey());
                if (isSingleLine(value)) {
                    println(color(key, YELLOW) + ": " + color(toLiteral(value), GREEN), indent + 4);
                } else {
                    println(color(key, YELLOW) + ":", indent + 4);
                    print(value, indent + 8);
                }
            }
            println("}", indent);
            return;
        }

        println(color(clazz.getSimpleName(), CYAN) + " {", indent);
        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null || value instanceof SourceLocation || isEmpty(value)) {
                    continue;
                }

                if (!field.getName().equals("parent")) {
                    String name = color(field.getName(), YELLOW);
                    if (isSingleLine(value)) {
                        println(name + ": " + color(toLiteral(value), GREEN), indent + 4);
                    } else {
                        println(name + ":", indent + 4);
                        print(value, indent + 8);
                    }
                }
            } catch (IllegalAccessException e) {
                println(field.getName() + ": [error]", indent + 4);
            }
        }
        println("}", indent);
    }

    private static boolean isEmpty(Object obj) {
        switch (obj) {
            case null -> {
                return true;
            }
            case Collection<?> c -> {
                return c.isEmpty();
            }
            case Map<?, ?> m -> {
                return m.isEmpty();
            }
            default -> {
            }
        }
        if (obj.getClass().isArray()) return Array.getLength(obj) == 0;
        return false;
    }

    private static boolean isSingleLine(Object value) {
        return value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof Token
                || value instanceof axl.compiler.linker.type.data.Type;
    }

    private static String toLiteral(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String s) return "\"" + s + "\"";
        return obj.toString();
    }

    private static void println(String text, int indent) {
        System.out.println(" ".repeat(indent) + text + RESET);
    }

    private static String color(String value, String color) {
        return color + value + RESET;
    }

    @SneakyThrows
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
            type = type.getSuperclass();
        }
        return fields;
    }
}
