package axl.compiler.linker.type.data;

import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.MethodDescriptor;

import java.util.*;

public interface Type extends Cloneable {

    String getName();

    String getSuperClass();

    List<String> getSuperClassTypeParameters();

    List<String> getInterfaces();

    Map<String, List<String>> getInterfacesTypeParameters();

    List<MethodDescriptor> getMethods();

    List<FieldDescriptor> getFields();

    List<String> getTypeParameters();

    TypeKind getKind();

    Type clone();

    default String getDescriptor() {
        return switch (getKind()) {
            case PRIMITIVE -> getInternalName();
            case CLASS, INTERFACE -> "L" + getInternalName() + ";";
            default -> throw new IllegalStateException("Unsupported kind for descriptor: " + getKind());
        };
    }

    default String getInternalName() {
        return switch (getKind()) {
            case PRIMITIVE -> switch (getName()) {
                case "int" -> "I";
                case "long" -> "J";
                case "float" -> "F";
                case "double" -> "D";
                case "boolean" -> "Z";
                case "char" -> "C";
                case "short" -> "S";
                case "byte" -> "B";
                case "void" -> "V";
                default -> throw new IllegalStateException("Unknown primitive: " + getName());
            };
            case CLASS, INTERFACE -> getName().replace('.', '/');
            default -> throw new IllegalStateException("Unknown type kind: " + getKind());
        };
    }

}
