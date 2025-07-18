package axl.compiler.analysis.semantic.data.scope;

import axl.compiler.analysis.semantic.data.tree.DeclarationType;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;
import axl.compiler.linker.type.data.PrimitiveType;
import axl.compiler.linker.type.data.Type;
import lombok.*;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class Scope {

    private final Scope parent;

    private final HashSet<String> initialized;

    private final Map<String, SymbolElt> symbols = new HashMap<>();

    private final List<Scope> children = new ArrayList<>();

    private int nextIndex = 0;

    public Scope createChild() {
        Scope child = new Scope(this, new HashSet<>(initialized));
        children.add(child);
        return child;
    }

    public void declare(String name, Type type, DeclarationType declarationType) {
        if (symbols.containsKey(name)) {
            throw new SemanticException("Variable '" + name + "' already declared in this scope");
        }

        int size = getSlotSize(type);
        SymbolElt symbol = new SymbolElt(name, type, declarationType, nextIndex, size, false);
        nextIndex += size;
        symbols.put(name, symbol);
    }

    public boolean initialize(String name) {
        resolve(name);
        return initialized.add(name);
    }

    public SymbolElt resolve(String name) {
        Scope current = this;
        while (current != null) {
            SymbolElt s = current.symbols.get(name);
            if (s != null) return s;
            current = current.parent;
        }
        throw new SemanticException("Variable '" + name + "' is not declared");
    }

    public int getStackIndex(String name) {
        return resolve(name).getIndex();
    }

    public Type getType(String name) {
        return resolve(name).getType();
    }

    private int getSlotSize(Type type) {
        return switch (type) {
            case PrimitiveType primitiveType -> switch (primitiveType.getName()) {
                case "long", "double" -> 2;
                default -> 1;
            };
            default -> 1;
        };
    }
}
