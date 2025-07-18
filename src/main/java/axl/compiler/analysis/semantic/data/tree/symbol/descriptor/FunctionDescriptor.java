package axl.compiler.analysis.semantic.data.tree.symbol.descriptor;

import axl.compiler.linker.type.data.Type;
import lombok.Data;

import java.util.List;

@Data
public class FunctionDescriptor {

    private int accessFlags;

    private String name;

    private List<Type> parameterTypes;

    private Type returnType;
}
