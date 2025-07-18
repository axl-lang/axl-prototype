package axl.compiler.analysis.semantic.data.tree.elt;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.semantic.data.tree.DeclarationType;
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
public class DeclarationElt extends Value implements Elt {

    private DeclarationType declarationType;

    private Token name;

    private Type type;

    private Value value;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        if (value != null) {
            value.codegen(visitor, labels);

            int i = Generator.getScopes().peek().getStackIndex(name.getValue());
            if (value.getResult() instanceof PrimitiveType primitiveType) {
                switch (primitiveType.getDescriptor()) {
                    case "I", "S", "C", "B", "Z" -> visitor.visitIntInsn(Opcodes.ISTORE, i);
                    case "J" -> visitor.visitIntInsn(Opcodes.LSTORE, i);
                    case "F" -> visitor.visitIntInsn(Opcodes.FSTORE, i);
                    case "D" -> visitor.visitIntInsn(Opcodes.DSTORE, i);
                }
            } else {
                visitor.visitIntInsn(Opcodes.ASTORE, i);
            }
        }
    }

    public Type getType() {
        if (type != null)
            return type;

        return value.getResult();
    }

    @Override
    public Type getResult() {
        return null;
    }
}
