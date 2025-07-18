package axl.compiler.analysis.semantic.impl.semantic.type;

import axl.compiler.analysis.parser.data.reference.TypeReference;
import axl.compiler.analysis.semantic.data.scope.Scope;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.analysis.semantic.impl.semantic.SemanticVisitor;
import axl.compiler.linker.type.TypeRegistry;
import axl.compiler.linker.type.TypeUtils;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.data.VirtualType;

import java.util.Optional;
import java.util.Stack;

public interface TypeVisitor extends SemanticVisitor {

    default Optional<Type> resolve(String type) {
        return SemanticAnalyzerImpl.getLinkerContext().getResolver().resolve(type);
    }

    default Optional<Type> resolve(TypeReference type) {
        return SemanticAnalyzerImpl.getLinkerContext().getResolver().resolve(TypeUtils.fromTypeReference(type));
    }

    default TypeRegistry getRegistry() {
        return SemanticAnalyzerImpl.getLinkerContext().getRegistry();
    }

    default Type getVariableType(String name) {
        return SemanticAnalyzerImpl.getScopes().peek().getType(name);
    }

    default VirtualType getContext() {
        return (VirtualType) SemanticAnalyzerImpl.getContext().peek();
    }

    default Stack<Scope> scopes() {
        return SemanticAnalyzerImpl.getScopes();
    }
}
