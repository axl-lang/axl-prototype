package axl.compiler.analysis.semantic.impl.transform.symbol;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.declaration.ClassDeclaration;
import axl.compiler.analysis.semantic.data.tree.symbol.ClassSymbol;
import axl.compiler.analysis.semantic.impl.transform.TransformVisitor;

import java.util.ArrayList;

public class ClassSymbolVisitor implements TransformVisitor {

    @Override
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(node instanceof ClassDeclaration declaration))
            return;

        ClassSymbol symbol = new ClassSymbol();
        accept(declaration, symbol);

        symbol.setType(resolve(declaration.getClassReference().getValue().getFirst().getValue()).orElseThrow());
        symbol.setSymbols(new ArrayList<>());

        for (Declaration child : declaration.getDeclarations()) {
            setParent(child, symbol);
            treeAnalyzer.enqueue(child);
        }
    }
}
