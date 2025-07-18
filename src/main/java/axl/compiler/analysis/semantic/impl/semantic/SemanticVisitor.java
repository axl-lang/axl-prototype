package axl.compiler.analysis.semantic.impl.semantic;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.common.util.Visitor;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.elt.DeclarationElt;
import axl.compiler.analysis.semantic.data.tree.elt.FrameElt;
import axl.compiler.analysis.semantic.data.tree.elt.condition.ConditionElt;
import axl.compiler.analysis.semantic.data.tree.elt.condition.ForeachElt;
import axl.compiler.analysis.semantic.data.tree.elt.condition.WhileElt;
import axl.compiler.analysis.semantic.data.tree.elt.operation.ReturnElt;
import axl.compiler.analysis.semantic.data.tree.elt.operation.ThrowElt;
import axl.compiler.analysis.semantic.data.tree.symbol.ClassSymbol;
import axl.compiler.analysis.semantic.data.tree.symbol.DeclarationSymbol;
import axl.compiler.analysis.semantic.data.tree.symbol.FunctionSymbol;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;
import axl.compiler.analysis.semantic.data.tree.value.*;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;

import java.util.List;

public interface SemanticVisitor extends Visitor {

    @Override
    default void enter(TreeAnalyzer treeAnalyzer, Object node) {
        Visitor.super.enter(treeAnalyzer, node);
        switch (node) {
            case ModuleSymbol ignored -> {
                SemanticAnalyzerImpl.getContext().push(SemanticAnalyzerImpl.getLinkerContext().getContext());
            }
            case ClassSymbol classSymbol -> {
                SemanticAnalyzerImpl.getContext().push(classSymbol.getType());
            }
            case FrameElt frameElt -> {
                SemanticAnalyzerImpl.getScopes().push(frameElt.getScope());
            }
            default -> {}
        }
    }

    @Override
    default void exit(TreeAnalyzer treeAnalyzer, Object node) {
        Visitor.super.exit(treeAnalyzer, node);
        switch (node) {
            case ModuleSymbol ignored -> {
                SemanticAnalyzerImpl.getContext().pop();
            }
            case ClassSymbol ignored -> {
                SemanticAnalyzerImpl.getContext().pop();
            }
            case FrameElt ignored -> {
                SemanticAnalyzerImpl.getScopes().pop();
            }
            default -> {}
        }
    }

    default void enqueue(TreeAnalyzer treeAnalyzer, SemanticNode current) {
        switch (current) {
            case DeclarationElt declarationElt -> {
                enqueuePart(treeAnalyzer, declarationElt.getValue(), current);
            }
            case FrameElt frameElt -> {
                enqueuePart(treeAnalyzer, frameElt.getValue(), current);
            }
            case ConditionElt conditionElt -> {
                enqueuePart(treeAnalyzer, conditionElt.getCondition(), current);
                enqueuePart(treeAnalyzer, conditionElt.getThen(), current);
                enqueuePart(treeAnalyzer, conditionElt.getAlso(), current);
            }
            case ForeachElt foreachElt -> {
                enqueuePart(treeAnalyzer, foreachElt.getIterator(), current);
                enqueuePart(treeAnalyzer, foreachElt.getThen(), current);
            }
            case WhileElt whileElt -> {
                enqueuePart(treeAnalyzer, whileElt.getCondition(), current);
                enqueuePart(treeAnalyzer, whileElt.getThen(), current);
            }
            case ReturnElt returnElt -> {
                enqueuePart(treeAnalyzer, returnElt.getValue(), current);
            }
            case ThrowElt throwElt -> {
                enqueuePart(treeAnalyzer, throwElt.getValue(), current);
            }
            case ClassSymbol classSymbol -> {
                enqueuePart(treeAnalyzer, classSymbol.getSymbols(), current);
            }
            case DeclarationSymbol declarationSymbol -> {
                enqueuePart(treeAnalyzer, declarationSymbol.getValue(), current);
            }
            case FunctionSymbol functionSymbol -> {
                enqueuePart(treeAnalyzer, functionSymbol.getBody(), current);
            }
            case ModuleSymbol moduleSymbol -> {
                enqueuePart(treeAnalyzer, moduleSymbol.getSymbols(), current);
            }
            case BinaryValue binaryValue -> {
                enqueuePart(treeAnalyzer, binaryValue.getLeft(), current);
                enqueuePart(treeAnalyzer, binaryValue.getRight(), current);
            }
            case InvokeStaticValue invokeStaticValue -> {
                enqueuePart(treeAnalyzer, invokeStaticValue.getArguments(), current);
            }
            case InvokeValue invokeValue -> {
                enqueuePart(treeAnalyzer, invokeValue.getSource(), current);
                enqueuePart(treeAnalyzer, invokeValue.getArguments(), current);
            }
            case NewValue newValue -> {
                enqueuePart(treeAnalyzer, newValue.getArguments(), current);
            }
            case UnaryValue unaryValue -> {
                enqueuePart(treeAnalyzer, unaryValue.getValue(), current);
            }
            default -> {}
        }
    }

    default void enqueuePart(TreeAnalyzer treeAnalyzer, Object child, SemanticNode parent) {
        if (!(child instanceof SemanticNode node)) {
            if (child instanceof List<?>) {
                for (Object o: (List<?>) child) {
                    enqueuePart(treeAnalyzer, o, parent);
                }
            }
            return;
        }

        if (node.getParent() == null) {
            node.setParent(parent);
        }

        treeAnalyzer.enqueue(node);
    }
}
