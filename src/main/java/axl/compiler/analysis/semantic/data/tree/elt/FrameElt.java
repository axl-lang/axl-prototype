package axl.compiler.analysis.semantic.data.tree.elt;

import axl.compiler.analysis.semantic.data.scope.Scope;
import axl.compiler.analysis.semantic.data.scope.SymbolElt;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.codegen.Generator;
import lombok.Data;
import lombok.Getter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Stack;

@Data
public class FrameElt extends SemanticNode implements Elt {

    private Scope scope;

    private List<Elt> value;

    @Getter
    private boolean localFinally = false;

    @Getter
    private boolean globalFinally = false;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {

        Label start = new Label();
        Label end = new Label();

        for (SymbolElt symbolElt: scope.getSymbols().values()) {
            visitor.visitLocalVariable(
                    symbolElt.getName(),
                    symbolElt.getType().getDescriptor(),
                    null,
                    start,
                    end,
                    symbolElt.getIndex()
            );
        }

        visitor.visitLabel(start);
        Generator.getScopes().push(scope);
        value.forEach(elt -> {
            elt.codegen(visitor, labels);
            if (elt instanceof Value value && value.getResult() != null && value.getResult().getName() != "void") {
                visitor.visitInsn(Opcodes.POP);
            }
        });
        Generator.getScopes().pop();
        visitor.visitLabel(end);
    }
}
