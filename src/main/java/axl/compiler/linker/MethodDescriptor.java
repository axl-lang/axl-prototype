package axl.compiler.linker;

import lombok.*;
import java.lang.reflect.Modifier;
import java.util.*;

@Data
@AllArgsConstructor
public class MethodDescriptor implements Cloneable {

    private final String name;

    private List<String> parameters;

    private String returnType;

    private int access;

    @Override
    public String toString() {
        return name + parameters + ": " + returnType + " (" + Modifier.toString(access) + ")";
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public MethodDescriptor clone() {
        return new MethodDescriptor(
                name,
                parameters != null ? new ArrayList<>(parameters) : null,
                returnType,
                access
        );
    }
}
