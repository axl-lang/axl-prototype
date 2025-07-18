package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
public class UnaryValue extends Value implements Elt {

    private Value value;

    private Token operator;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        TokenType type = operator.getType();

        switch (type) {
            case UNARY_MINUS -> {
                value.codegen(visitor, labels);
                switch (value.getResult().getDescriptor()) {
                    case "I", "S", "C", "B", "Z" -> visitor.visitInsn(Opcodes.INEG);
                    case "J" -> visitor.visitInsn(Opcodes.LNEG);
                    case "F" -> visitor.visitInsn(Opcodes.FNEG);
                    case "D" -> visitor.visitInsn(Opcodes.DNEG);
                }
            }

            case NOT -> {
                value.codegen(visitor, labels);
                Label trueLabel = new Label();
                Label endLabel = new Label();
                visitor.visitJumpInsn(Opcodes.IFEQ, trueLabel);
                visitor.visitInsn(Opcodes.ICONST_0);
                visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
                visitor.visitLabel(trueLabel);
                visitor.visitInsn(Opcodes.ICONST_1);
                visitor.visitLabel(endLabel);
            }

            default -> throw new UnsupportedOperationException("Unsupported unary operator: " + type);
        }
    }

}
