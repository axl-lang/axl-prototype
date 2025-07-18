package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.codegen.Generator;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Stack;

@Data
public class SingleValue extends Value implements Elt {

    private Token value;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        switch (value.getType()) {
            case TRUE -> visitor.visitInsn(org.objectweb.asm.Opcodes.ICONST_1);
            case FALSE -> visitor.visitInsn(org.objectweb.asm.Opcodes.ICONST_0);

            case DEC_NUMBER, HEX_NUMBER -> {
                int intValue = Integer.decode(value.getValue());
                emitIntConstant(visitor, intValue);
            }

            case DEC_LONG_NUMBER, HEX_LONG_NUMBER -> {
                long longValue = Long.decode(value.getValue().replace("L", ""));
                visitor.visitLdcInsn(longValue);
            }

            case FLOAT_NUMBER -> {
                float floatValue = Float.parseFloat(value.getValue().replace("f", ""));
                visitor.visitLdcInsn(floatValue);
            }

            case DOUBLE_NUMBER -> {
                double doubleValue = Double.parseDouble(value.getValue());
                visitor.visitLdcInsn(doubleValue);
            }

            case CHAR_LITERAL -> {
                char ch = parseCharLiteral(value.getValue());
                emitIntConstant(visitor, (int) ch);
            }

            case STRING_LITERAL -> {
                String str = parseStringLiteral(value.getValue());
                visitor.visitLdcInsn(str);
            }

            case IDENTIFY -> {
                if (getParent() instanceof InvokeValue invokeValue && invokeValue.getSource() == this)
                    return;

                VariableValue variableValue = new VariableValue();
                variableValue.setName(value.getValue());
                variableValue.setType(Generator.getScopes().peek().getType(value.getValue()));
                variableValue.codegen(visitor, labels);
            }

            default -> throw new UnsupportedOperationException("Unsupported literal: " + value.getType());
        }
    }

    private void emitIntConstant(MethodVisitor visitor, int value) {
        if (value >= -1 && value <= 5) {
            visitor.visitInsn(org.objectweb.asm.Opcodes.ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            visitor.visitIntInsn(org.objectweb.asm.Opcodes.BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            visitor.visitIntInsn(org.objectweb.asm.Opcodes.SIPUSH, value);
        } else {
            visitor.visitLdcInsn(value);
        }
    }

    private char parseCharLiteral(String text) {
        if (text.startsWith("'") && text.endsWith("'")) {
            String core = text.substring(1, text.length() - 1);
            if (core.length() == 1) return core.charAt(0);
            if (core.startsWith("\\")) {
                switch (core.charAt(1)) {
                    case 'n' -> { return '\n'; }
                    case 't' -> { return '\t'; }
                    case 'r' -> { return '\r'; }
                    case '\'' -> { return '\''; }
                    case '"' -> { return '"'; }
                    case '\\' -> { return '\\'; }
                    default -> throw new IllegalArgumentException("Unknown escape: " + core);
                }
            }
        }
        throw new IllegalArgumentException("Invalid char literal: " + text);
    }

    private String parseStringLiteral(String text) {
        if (text.startsWith("\"") && text.endsWith("\"")) {
            String core = text.substring(1, text.length() - 1);
            return core
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\r", "\r")
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .replace("\\\\", "\\");
        }
        throw new IllegalArgumentException("Invalid string literal: " + text);
    }
}
