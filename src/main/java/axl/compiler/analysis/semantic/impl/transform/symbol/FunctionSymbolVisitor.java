package axl.compiler.analysis.semantic.impl.transform.symbol;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.parser.data.declaration.ArgumentDeclaration;
import axl.compiler.analysis.parser.data.declaration.FunctionDeclaration;
import axl.compiler.analysis.semantic.data.tree.symbol.FunctionSymbol;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;
import axl.compiler.analysis.semantic.data.tree.symbol.descriptor.FunctionDescriptor;
import axl.compiler.analysis.semantic.impl.transform.TransformVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;

public class FunctionSymbolVisitor implements TransformVisitor {

    @Override
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(node instanceof FunctionDeclaration declaration))
            return;

        FunctionSymbol symbol = new FunctionSymbol();
        accept(declaration, symbol);

        FunctionDescriptor descriptor = new FunctionDescriptor();
        descriptor.setName(declaration.getName().getValue());
        descriptor.setAccessFlags(computeAccessFlags(declaration));
        descriptor.setReturnType(resolve(declaration.getResultReference()).orElseThrow());
        descriptor.setParameterTypes(
                declaration.getArgumentDeclarations().stream()
                .map(ArgumentDeclaration::getReference)
                .map(this::resolve)
                .map(Optional::orElseThrow)
                .toList()
        );
        symbol.setDescriptor(descriptor);
        symbol.setDeclaration(declaration);

        setParent(declaration.getBody(), symbol);
        treeAnalyzer.enqueue(declaration.getBody());
    }

    private int computeAccessFlags(FunctionDeclaration declaration) {
        int flags = 0;

        if (declaration.getParent() instanceof ModuleSymbol) {
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
}
