package axl.compiler.analysis.semantic.data.tree.symbol;

import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Symbol;
import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.type.data.Type;
import lombok.Data;

import java.util.List;

@Data
public class ClassSymbol extends SemanticNode implements Symbol {

    private Type type;

    private List<Symbol> symbols;

    private List<FieldDescriptor> fields;
}
