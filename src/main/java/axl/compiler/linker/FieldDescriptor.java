package axl.compiler.linker;

import lombok.*;
import java.lang.reflect.Modifier;

@Data
@AllArgsConstructor
public class FieldDescriptor implements Cloneable {

    private final String name;

    private String type;

    private int access;

    @Override
    public String toString() {
        return name + ": " + type + " (" + Modifier.toString(access) + ")";
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public FieldDescriptor clone() {
        return new FieldDescriptor(name, type, access);
    }
}
