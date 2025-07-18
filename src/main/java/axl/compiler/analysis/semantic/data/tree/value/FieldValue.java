package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.codegen.Generator;
import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.type.data.Type;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
public class FieldValue extends Value implements Elt {

    private Value source;

    private String name;

    private Type type;

    private Type context;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        FieldDescriptor field = context.getFields().stream()
                .filter(f -> f.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Field not found: " + name + " in " + context.getName()));

        Type ownerType;
        if ((field.getAccess() & Opcodes.ACC_STATIC) != 0) {
            ownerType = context;
            visitor.visitFieldInsn(Opcodes.GETSTATIC, ownerType.getInternalName(), name, type.getDescriptor());
        } else {
            if (source == null) {
                int thisIndex = Generator.getScopes().peek().getStackIndex("this");
                visitor.visitVarInsn(Opcodes.ALOAD, thisIndex);
                ownerType = context;
            } else {
                source.codegen(visitor, labels);
                ownerType = source.getResult();
            }
            visitor.visitFieldInsn(Opcodes.GETFIELD, ownerType.getInternalName(), name, type.getDescriptor());
        }
    }
}
