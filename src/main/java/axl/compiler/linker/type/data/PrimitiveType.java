package axl.compiler.linker.type.data;

import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.MethodDescriptor;

import java.util.List;
import java.util.Map;

public class PrimitiveType implements Type {

    private final String name;

    public PrimitiveType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSuperClass() {
        return null;
    }

    @Override
    public List<String> getSuperClassTypeParameters() {
        return null;
    }

    @Override
    public List<String> getInterfaces() {
        return null;
    }

    @Override
    public Map<String, List<String>> getInterfacesTypeParameters() {
        return null;
    }

    @Override
    public List<MethodDescriptor> getMethods() {
        return null;
    }

    @Override
    public List<FieldDescriptor> getFields() {
        return null;
    }

    @Override
    public List<String> getTypeParameters() {
        return null;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.PRIMITIVE;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Type clone() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PrimitiveType && ((PrimitiveType) obj).getName().equals(getName());
    }

    @Override
    public String toString() {
        return name;
    }
}
