package axl.compiler.analysis.semantic.data.scope;

import axl.compiler.analysis.semantic.data.tree.DeclarationType;
import axl.compiler.linker.type.data.Type;
import lombok.*;

@Data
@AllArgsConstructor
public class SymbolElt implements Cloneable {

    private final String name;

    private final Type type;

    private final DeclarationType declarationType;

    private final int index;

    private final int size;

    private boolean initialized;

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public SymbolElt clone() {
        return new SymbolElt(name, type, declarationType, index, size, initialized);
    }
}
