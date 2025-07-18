package axl.compiler.linker.type.data;

import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.MethodDescriptor;
import lombok.*;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class VirtualType implements Type {

    private final String name;

    private String superClass;

    private List<String> superClassTypeParameters;

    private List<String> interfaces;

    private Map<String, List<String>> interfacesTypeParameters;

    private List<MethodDescriptor> methods;

    private List<FieldDescriptor> fields;

    private List<String> typeParameters;

    private TypeKind kind;

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public VirtualType clone() {
        return new VirtualType(
                name,
                superClass,
                superClassTypeParameters != null ? new ArrayList<>(superClassTypeParameters) : null,
                interfaces != null ? new ArrayList<>(interfaces) : null,
                interfacesTypeParameters != null ? deepCopyMap(interfacesTypeParameters) : null,
                methods != null ? methods.stream().map(MethodDescriptor::clone).toList() : null,
                fields != null ? fields.stream().map(FieldDescriptor::clone).toList() : null,
                typeParameters != null ? new ArrayList<>(typeParameters) : null,
                kind
        );
    }

    private static Map<String, List<String>> deepCopyMap(Map<String, List<String>> original) {
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VirtualType && ((VirtualType) obj).getName().equals(getName());
    }

    @Override
    public String toString() {
        return getName();
    }

    public List<String> getTypeArguments() {
        return new ArrayList<>();
    }
}
