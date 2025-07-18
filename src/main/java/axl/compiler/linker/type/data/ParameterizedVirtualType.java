package axl.compiler.linker.type.data;

import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.MethodDescriptor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ParameterizedVirtualType extends VirtualType {

    private List<String> typeArguments;

    public ParameterizedVirtualType(String name,
                                    String superClass,
                                    List<String> superClassTypeParameters,
                                    List<String> interfaces,
                                    Map<String, List<String>> interfacesTypeParameters,
                                    List<MethodDescriptor> methods,
                                    List<FieldDescriptor> fields,
                                    List<String> typeParameters,
                                    List<String> typeArguments,
                                    TypeKind kind) {
        super(name, superClass, superClassTypeParameters, interfaces, interfacesTypeParameters, methods, fields, typeParameters, kind);
        this.typeArguments = typeArguments;
    }

    public ParameterizedVirtualType(Type base, List<String> mapped) {
        this(
                base.getName(),
                base.getSuperClass(),
                base.getSuperClassTypeParameters(),
                base.getInterfaces(),
                base.getInterfacesTypeParameters(),
                base.getMethods(),
                base.getFields(),
                base.getTypeParameters(),
                mapped,
                base.getKind()
        );
    }

    @Override
    public ParameterizedVirtualType clone() {
        VirtualType base = super.clone();
        return new ParameterizedVirtualType(
                base.getName(),
                base.getSuperClass(),
                base.getSuperClassTypeParameters(),
                base.getInterfaces(),
                base.getInterfacesTypeParameters(),
                base.getMethods(),
                base.getFields(),
                base.getTypeParameters(),
                typeArguments != null ? new ArrayList<>(typeArguments) : null,
                base.getKind()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterizedVirtualType p)
            return getName().equals(p.getName()) && getTypeArguments().equals(p.getTypeArguments());

        if (o instanceof VirtualType)
            return getName().equals(((VirtualType) o).getName());

        return false;
    }

    @Override
    public String toString() {
        if (getTypeArguments().isEmpty())
            return getName();

        return getName() + "<" + String.join(",", getTypeArguments()) + ">";
    }
}
