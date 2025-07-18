package axl.compiler.analysis.semantic;

import axl.compiler.analysis.parser.data.declaration.ModuleDeclaration;
import axl.compiler.analysis.semantic.data.context.LinkerContext;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;

public interface SemanticAnalyzer {

    LinkerContext analyze(ModuleDeclaration declaration);

    ModuleSymbol transform(ModuleDeclaration declaration);
}
