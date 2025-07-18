package axl.compiler.analysis.semantic.impl.transform;

import axl.compiler.analysis.common.util.Visitor;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Symbol;
import axl.compiler.analysis.semantic.data.tree.Value;
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
import axl.compiler.linker.type.TypeUtils;
import axl.compiler.linker.type.data.Type;

import java.util.Optional;

public interface TransformVisitor extends Visitor {

    default Optional<Type> resolve(String type) {
        return SemanticAnalyzerImpl.getLinkerContext().getResolver().resolve(type);
    }

    default Optional<Type> resolve(TypeReference type) {
        return SemanticAnalyzerImpl.getLinkerContext().getResolver().resolve(TypeUtils.fromTypeReference(type));
    }

    default void setParent(Node node, Object parent) {
        if (node == null)
            return;

        node.setParent(parent);
    }

    default void accept(Node first, SemanticNode child) {
        if (first.getParent() instanceof SemanticNode semanticNode) {
            switch (semanticNode) {
                case ModuleSymbol moduleSymbol -> {
                    moduleSymbol.getSymbols().add((Symbol) child);
                }
                case DeclarationElt declarationElt -> {
                    declarationElt.setValue((Value) child);
                }
                case FrameElt frameElt -> {
                    frameElt.getValue().add((Elt) child);
                }
                case ConditionElt conditionElt -> {
                    if (child instanceof Value) {
                        conditionElt.setCondition((Value) child);
                    } else if (conditionElt.getThen() == null) {
                        conditionElt.setThen((FrameElt) child);
                    } else {
                        conditionElt.setAlso((FrameElt) child);
                    }
                }
                case ForeachElt foreachElt -> {
                    if (child instanceof DeclarationElt) {
                        foreachElt.setDeclaration((DeclarationElt) child);
                    } else if (child instanceof Value) {
                        foreachElt.setIterator((Value) child);
                    } else {
                        foreachElt.setThen((FrameElt) child);
                    }
                }
                case WhileElt whileElt -> {
                    if (child instanceof Value) {
                        whileElt.setCondition((Value) child);
                    } else {
                        whileElt.setThen((FrameElt) child);
                    }
                }
                case ReturnElt returnElt -> {
                    returnElt.setValue((Value) child);
                }
                case ThrowElt throwElt -> {
                    throwElt.setValue((Value) child);
                }
                case ClassSymbol classSymbol -> {
                    classSymbol.getSymbols().add((Symbol) child);
                }
                case FunctionSymbol functionSymbol -> {
                    functionSymbol.setBody((FrameElt) child);
                }
                case DeclarationSymbol valueSymbol -> {
                    valueSymbol.setValue((Value) child);
                }
                case BinaryValue binaryValue -> {
                    if (binaryValue.getLeft() == null) {
                        binaryValue.setLeft((Value) child);
                    } else {
                        binaryValue.setRight((Value) child);
                    }
                }
                case UnaryValue unaryValue -> {
                    unaryValue.setValue((Value) child);
                }
                case InvokeValue invokeValue -> {
                    if (invokeValue.getSource() == null) {
                        invokeValue.setSource((Value) child);
                    } else {
                        invokeValue.getArguments().add((Value) child);
                    }
                }
                case InvokeStaticValue invokeStaticValue -> {
                    invokeStaticValue.getArguments().add((Value) child);
                }
                case NewValue newValue -> {
                    newValue.getArguments().add((Value) child);
                }
                default -> {

                }
            }
        }
    }
}
