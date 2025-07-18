package axl.compiler.analysis.semantic.data.tree.elt.operation;

import axl.compiler.analysis.semantic.data.tree.Value;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThrowElt extends OperationElt {

    private Value value;

    @Override
    public boolean isGlobalFinally() {
        return true;
    }

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        value.codegen(visitor, labels);
        visitor.visitInsn(Opcodes.ATHROW);
    }
}
