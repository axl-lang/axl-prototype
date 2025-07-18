package axl.compiler.analysis.semantic.impl.linker;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.common.util.Visitor;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.declaration.ClassDeclaration;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.data.TypeKind;
import axl.compiler.linker.type.TypeUtils;
import axl.compiler.linker.type.data.VirtualType;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClassContextVisitor implements Visitor {

    @Override
    @SneakyThrows
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(Objects.requireNonNull(node) instanceof ClassDeclaration declaration))
            return;

        Type type = createVirtualType(declaration);
        SemanticAnalyzerImpl.getLinkerContext().getRegistry().getTypes().put(type.getName(), type);
        SemanticAnalyzerImpl.getLinkerContext().getClassContexts().push(type);

        for (Declaration child : declaration.getDeclarations()) {
            treeAnalyzer.enqueue(child);
        }
    }

    @Override
    public void exit(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(Objects.requireNonNull(node) instanceof ClassDeclaration))
            return;

        Type classType = SemanticAnalyzerImpl.getLinkerContext().getClassContexts().pop();
        SemanticAnalyzerImpl.getLinkerContext().getProcessedClassContexts().add(classType);
    }

    private VirtualType createVirtualType(ClassDeclaration declaration) {
        String packageLocation = SemanticAnalyzerImpl.getLinkerContext().getPackageLocation();
        if (packageLocation != null)
            packageLocation += ".";

        return new VirtualType(
                packageLocation + declaration.getClassReference().getValue().getFirst().getValue(),
                declaration.getSuperClassReference() == null ? "java.lang.Object" : TypeUtils.fromTypeReference(declaration.getSuperClassReference()),
                List.of(),
                List.of(),
                Map.of(),
                new ArrayList<>(),
                new ArrayList<>(),
                List.of(),
                TypeKind.CLASS
        );
    }
}
