package axl.compiler.analysis.semantic.impl.transform.symbol;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.declaration.ModuleDeclaration;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;
import axl.compiler.analysis.semantic.impl.transform.TransformVisitor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class ModuleSymbolVisitor implements TransformVisitor {

    private final ModuleSymbol symbol;

    @Override
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(node instanceof ModuleDeclaration declaration))
            return;

        symbol.setSymbols(new ArrayList<>());

        for (Declaration child : declaration.getDeclarations()) {
            setParent(child, symbol);
            treeAnalyzer.enqueue(child);
        }
    }
}
