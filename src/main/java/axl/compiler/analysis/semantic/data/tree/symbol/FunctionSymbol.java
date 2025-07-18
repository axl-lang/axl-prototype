package axl.compiler.analysis.semantic.data.tree.symbol;

import axl.compiler.analysis.parser.data.declaration.FunctionDeclaration;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Symbol;
import axl.compiler.analysis.semantic.data.tree.elt.FrameElt;
import axl.compiler.analysis.semantic.data.tree.symbol.descriptor.FunctionDescriptor;
import lombok.Data;

@Data
public class FunctionSymbol extends SemanticNode implements Symbol {

    private FunctionDescriptor descriptor;

    private FunctionDeclaration declaration;

    private FrameElt body;
}
