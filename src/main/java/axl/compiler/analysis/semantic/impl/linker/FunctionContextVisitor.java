package axl.compiler.analysis.semantic.impl.linker;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.common.util.Visitor;
import axl.compiler.analysis.parser.data.declaration.ArgumentDeclaration;
import axl.compiler.analysis.parser.data.declaration.FunctionDeclaration;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.linker.MethodDescriptor;
import axl.compiler.linker.type.TypeUtils;
import lombok.SneakyThrows;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Objects;

public class FunctionContextVisitor implements Visitor {

    @Override
    @SneakyThrows
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(Objects.requireNonNull(node) instanceof FunctionDeclaration declaration))
            return;

        List<String> parameters = declaration.getArgumentDeclarations().stream()
                .map(ArgumentDeclaration::getReference)
                .map(TypeUtils::fromTypeReference)
                .toList();

        int accessFlag = computeAccessFlags(declaration);
        MethodDescriptor descriptor = new MethodDescriptor(
                declaration.getName().getValue(),
                parameters,
                TypeUtils.fromTypeReference(declaration.getResultReference()),
                accessFlag
        );

        getTargetMethodList().add(descriptor);
    }

    private int computeAccessFlags(FunctionDeclaration declaration) {
        int flags = 0;

        if (SemanticAnalyzerImpl.getLinkerContext().getClassContexts().isEmpty()) {
            flags |= Opcodes.ACC_STATIC;
        }

        if (declaration.getAccessFlag() != null) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (declaration.getAccessFlag().getType()) {
                case PUB -> flags |= Opcodes.ACC_PUBLIC;
                default -> flags |= Opcodes.ACC_PRIVATE;
            }
        }

        return flags;
    }

    private List<MethodDescriptor> getTargetMethodList() {
        var context = SemanticAnalyzerImpl.getLinkerContext();
        return context.getClassContexts().isEmpty()
                ? context.getContext().getMethods()
                : context.getClassContexts().peek().getMethods();
    }
}
