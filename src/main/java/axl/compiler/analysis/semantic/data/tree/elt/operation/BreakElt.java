package axl.compiler.analysis.semantic.data.tree.elt.operation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
@EqualsAndHashCode(callSuper = true)
public class BreakElt extends OperationElt {

    @Override
    public boolean isLocalFinally() {
        return true;
    }

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        visitor.visitJumpInsn(Opcodes.GOTO, labels.peek());
    }
}
