package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.codegen.Generator;
import axl.compiler.linker.type.data.PrimitiveType;
import axl.compiler.linker.type.data.Type;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
public class VariableValue extends Value implements Elt {

    private String name;

    private Type type;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        int i = Generator.getScopes().peek().getStackIndex(name);
        if (type instanceof PrimitiveType primitiveType) {
            switch (primitiveType.getDescriptor()) {
                case "I", "S", "C", "B", "Z" -> visitor.visitIntInsn(Opcodes.ILOAD, i);
                case "J" -> visitor.visitIntInsn(Opcodes.LLOAD, i);
                case "F" -> visitor.visitIntInsn(Opcodes.FLOAD, i);
                case "D" -> visitor.visitIntInsn(Opcodes.DLOAD, i);
            }
        } else {
            visitor.visitIntInsn(Opcodes.ALOAD, i);
        }
    }
}
