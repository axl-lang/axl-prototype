package axl.compiler.analysis.semantic.impl.transform.symbol;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.parser.data.declaration.ValDeclaration;
import axl.compiler.analysis.parser.data.declaration.VarDeclaration;
import axl.compiler.analysis.semantic.data.tree.DeclarationType;
import axl.compiler.analysis.semantic.data.tree.symbol.DeclarationSymbol;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;
import axl.compiler.analysis.semantic.impl.transform.TransformVisitor;
import org.objectweb.asm.Opcodes;

public class DeclarationSymbolVisitor implements TransformVisitor {

    @Override
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (node instanceof VarDeclaration declaration) {
            DeclarationSymbol declarationElt = new DeclarationSymbol();
            accept(declaration, declarationElt);

            if (declaration.getDeclarationFlag() != null) {
                declarationElt.setAccessFlag(Opcodes.ACC_PUBLIC);
            }

            if (declaration.getParent() instanceof ModuleSymbol) {
                declarationElt.setAccessFlag(declarationElt.getAccessFlag() | Opcodes.ACC_STATIC);
            }

            declarationElt.setDeclarationType(DeclarationType.VAR);
            declarationElt.setName(declaration.getName());

            if (declaration.getReference() != null) {
                declarationElt.setType(resolve(declaration.getReference()).orElseThrow());
            }

            if (declaration.getValue() != null) {
                setParent(declaration.getValue(), declarationElt);
                treeAnalyzer.enqueue(declaration.getValue());
            }
        }

        if (node instanceof ValDeclaration declaration) {
            DeclarationSymbol declarationElt = new DeclarationSymbol();
            accept(declaration, declarationElt);

            if (declaration.getDeclarationFlag() != null) {
                declarationElt.setAccessFlag(Opcodes.ACC_PUBLIC);
            }

            if (declaration.getParent() instanceof ModuleSymbol) {
                declarationElt.setAccessFlag(declarationElt.getAccessFlag() | Opcodes.ACC_STATIC);
            }

            declarationElt.setDeclarationType(DeclarationType.VAL);
            declarationElt.setName(declaration.getName());

            if (declaration.getReference() != null) {
                declarationElt.setType(resolve(declaration.getReference()).orElseThrow());
            }

            if (declaration.getValue() != null) {
                setParent(declaration.getValue(), declarationElt);
                treeAnalyzer.enqueue(declaration.getValue());
            }
        }
    }
}
