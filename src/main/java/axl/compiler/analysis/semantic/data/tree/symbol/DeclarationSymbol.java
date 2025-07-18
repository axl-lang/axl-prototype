package axl.compiler.analysis.semantic.data.tree.symbol;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.semantic.data.tree.*;
import axl.compiler.linker.type.data.Type;
import lombok.Data;

@Data
public class DeclarationSymbol extends SemanticNode implements Symbol {

    private int accessFlag;

    private DeclarationType declarationType;

    private Token name;

    private Type type;

    private Value value;
}
