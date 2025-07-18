package axl.compiler.analysis.semantic.data.tree.elt.operation;

import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.linker.type.data.PrimitiveType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReturnElt extends OperationElt {

    private Value value;

    @Override
    public boolean isGlobalFinally() {
        return true;
    }

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        if (value == null) {
            visitor.visitInsn(Opcodes.RETURN);
            return;
        }

        value.codegen(visitor, labels);

        if (value.getResult() instanceof PrimitiveType primitiveType) {
            switch (primitiveType.getDescriptor()) {
                case "I", "S", "C", "B", "Z" -> visitor.visitInsn(Opcodes.IRETURN);
                case "J" -> visitor.visitInsn(Opcodes.LRETURN);
                case "F" -> visitor.visitInsn(Opcodes.FRETURN);
                case "D" -> visitor.visitInsn(Opcodes.DRETURN);
            }
        } else {
            visitor.visitInsn(Opcodes.ARETURN);
        }
    }
}
