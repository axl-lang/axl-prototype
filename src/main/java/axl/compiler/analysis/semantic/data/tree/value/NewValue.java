package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.linker.type.data.Type;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Stack;

@Data
public class NewValue extends Value implements Elt {

    private Type source;

    private List<Value> arguments;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        String internalName = source.getInternalName();

        visitor.visitTypeInsn(Opcodes.NEW, internalName);
        visitor.visitInsn(Opcodes.DUP);

        if (arguments != null && !arguments.isEmpty()) {
            for (Value arg : arguments) {
                arg.codegen(visitor, labels);
            }
        }

        StringBuilder constructorDescriptor = new StringBuilder();
        constructorDescriptor.append('(');
        if (arguments != null) {
            for (Value arg : arguments) {
                Type argType = arg.getResult();
                if (argType == null) {
                    throw new IllegalStateException("Argument type is not resolved for constructor");
                }
                constructorDescriptor.append(argType.getDescriptor());
            }
        }
        constructorDescriptor.append(")V");

        visitor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                internalName,
                "<init>",
                constructorDescriptor.toString(),
                false
        );
    }

    @Override
    public Type getResult() {
        return source;
    }
}
