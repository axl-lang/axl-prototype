package axl.compiler.analysis.semantic.impl.linker;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.common.util.Visitor;
import axl.compiler.analysis.parser.data.declaration.ValDeclaration;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.type.TypeUtils;
import lombok.SneakyThrows;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Objects;

public class ValContextVisitor implements Visitor {

    @Override
    @SneakyThrows
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(Objects.requireNonNull(node) instanceof ValDeclaration declaration))
            return;

        int accessFlag = computeAccessFlags(declaration);
        FieldDescriptor descriptor = new FieldDescriptor(
                declaration.getName().getValue(),
                TypeUtils.fromTypeReference(declaration.getReference()),
                accessFlag | Opcodes.ACC_FINAL
        );

        getTargetFieldList().add(descriptor);
    }

    private int computeAccessFlags(ValDeclaration declaration) {
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

    private List<FieldDescriptor> getTargetFieldList() {
        var context = SemanticAnalyzerImpl.getLinkerContext();
        return context.getClassContexts().isEmpty()
                ? context.getContext().getFields()
                : context.getClassContexts().peek().getFields();
    }
}
