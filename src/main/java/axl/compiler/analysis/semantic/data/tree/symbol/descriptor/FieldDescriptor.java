package axl.compiler.analysis.semantic.data.tree.symbol.descriptor;

import axl.compiler.linker.type.data.Type;
import lombok.Data;

@Data
public class FieldDescriptor {

    private int accessFlags;

    private String name;
    
    private Type type;
}
