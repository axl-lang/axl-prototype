package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.codegen.Generator;
import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.type.data.PrimitiveType;
import axl.compiler.linker.type.data.Type;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
public class BinaryValue extends Value implements Elt {

    private Value left;

    private Value right;

    private Token operator;

    private int instruction;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        TokenType op = operator.getType();

        switch (op) {
            case ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN -> {
                if (!(left instanceof SingleValue || left instanceof FieldValue)) {
                    throw new UnsupportedOperationException("Left side must be variable or field");
                }

                if (op != TokenType.ASSIGN) {
                    loadLeftValue(visitor);
                    right.codegen(visitor, labels);
                    applyArithmetic(visitor, op);
                } else {
                    right.codegen(visitor, labels);
                }

                storeLeftValue(visitor);
            }

            case EQUALS, NOT_EQUALS, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL -> {
                left.codegen(visitor, labels);
                right.codegen(visitor, labels);

                Label trueLabel = new Label();
                Label endLabel = new Label();

                int opcode = switch (op) {
                    case EQUALS -> Opcodes.IF_ICMPEQ;
                    case NOT_EQUALS -> Opcodes.IF_ICMPNE;
                    case GREATER -> Opcodes.IF_ICMPGT;
                    case LESS -> Opcodes.IF_ICMPLT;
                    case GREATER_OR_EQUAL -> Opcodes.IF_ICMPGE;
                    case LESS_OR_EQUAL -> Opcodes.IF_ICMPLE;
                    default -> throw new IllegalStateException("Unexpected value: " + op);
                };

                visitor.visitJumpInsn(opcode, trueLabel);
                visitor.visitInsn(Opcodes.ICONST_0);
                visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
                visitor.visitLabel(trueLabel);
                visitor.visitInsn(Opcodes.ICONST_1);
                visitor.visitLabel(endLabel);
            }

            case IS -> {
                if (!(right instanceof TypeValue typeValue)) {
                    throw new UnsupportedOperationException("'is' operator needs type on right side");
                }
                left.codegen(visitor, labels);
                visitor.visitTypeInsn(Opcodes.INSTANCEOF, typeValue.getValue().getInternalName());
            }

            case AS -> {
                if (!(right instanceof TypeValue typeValue)) {
                    throw new UnsupportedOperationException("'as' operator needs type on right side");
                }
                left.codegen(visitor, labels);
                visitor.visitTypeInsn(Opcodes.CHECKCAST, typeValue.getValue().getInternalName());
            }

            case DOT -> {
                if (!(right instanceof SingleValue singleValue) || singleValue.getValue().getType() != TokenType.IDENTIFY) {
                    throw new UnsupportedOperationException("Right side of DOT must be an identifier");
                }

                String fieldName = singleValue.getValue().getValue();

                if (left instanceof TypeValue typeValue) {
                    Type type = typeValue.getValue();
                    FieldDescriptor field = type.getFields().stream()
                            .filter(f -> f.getName().equals(fieldName) && (f.getAccess() & Opcodes.ACC_STATIC) != 0)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Static field not found: " + fieldName));

                    visitor.visitFieldInsn(Opcodes.GETSTATIC,
                            type.getInternalName(),
                            field.getName(),
                            SemanticAnalyzerImpl.getLinkerContext().getResolver()
                                    .resolve(field.getType()).orElseThrow().getDescriptor());
                } else {
                    left.codegen(visitor, labels);
                    Type type = left.getResult();

                    FieldDescriptor field = type.getFields().stream()
                            .filter(f -> f.getName().equals(fieldName) && (f.getAccess() & Opcodes.ACC_STATIC) == 0)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Instance field not found: " + fieldName));

                    Type resolvedType = SemanticAnalyzerImpl.getLinkerContext().getResolver()
                            .resolve(field.getType()).orElseThrow();

                    visitor.visitFieldInsn(Opcodes.GETFIELD,
                            type.getInternalName(),
                            field.getName(),
                            resolvedType.getDescriptor());
                }
            }

            default -> {
                left.codegen(visitor, labels);
                right.codegen(visitor, labels);
                applyArithmetic(visitor, op);
            }
        }
    }

    private void applyArithmetic(MethodVisitor visitor, TokenType op) {
        int opcode = switch (op) {
            case PLUS, PLUS_ASSIGN -> Opcodes.IADD;
            case MINUS, MINUS_ASSIGN -> Opcodes.ISUB;
            case MULTIPLY, MULTIPLY_ASSIGN -> Opcodes.IMUL;
            case DIVIDE, DIVIDE_ASSIGN -> Opcodes.IDIV;
            case MODULO, MODULO_ASSIGN -> Opcodes.IREM;
            default -> throw new UnsupportedOperationException("Invalid arithmetic operator: " + op);
        };

        visitor.visitInsn(opcode);
    }

    private void loadLeftValue(MethodVisitor visitor) {
        if (left instanceof SingleValue variable) {
            int index = Generator.getScopes().peek().getStackIndex(variable.getValue().getValue());
            Type type = Generator.getScopes().peek().getType(variable.getValue().getValue());
            emitLoad(visitor, type, index);
        } else if (left instanceof FieldValue field) {
            loadObjectChain(visitor, field.getSource());
            visitor.visitFieldInsn(Opcodes.GETFIELD,
                    field.getContext().getInternalName(),
                    field.getName(),
                    field.getType().getDescriptor());
        } else {
            throw new UnsupportedOperationException("Unsupported left side: " + left.getClass());
        }
    }

    private void storeLeftValue(MethodVisitor visitor) {
        if (left instanceof SingleValue variable) {
            int index = Generator.getScopes().peek().getStackIndex(variable.getValue().getValue());
            Type type = Generator.getScopes().peek().getType(variable.getValue().getValue());
            emitStore(visitor, type, index);
        } else if (left instanceof FieldValue field) {
            loadObjectChain(visitor, field.getSource());
            visitor.visitInsn(Opcodes.SWAP);
            visitor.visitFieldInsn(Opcodes.PUTFIELD,
                    field.getContext().getInternalName(),
                    field.getName(),
                    field.getType().getDescriptor());
        } else {
            throw new UnsupportedOperationException("Unsupported left side: " + left.getClass());
        }
    }

    private void loadObjectChain(MethodVisitor visitor, Value object) {
        switch (object) {
            case null -> {
                int thisIndex = Generator.getScopes().peek().getStackIndex("this");
                visitor.visitVarInsn(Opcodes.ALOAD, thisIndex);
            }
            case VariableValue variable -> {
                int idx = Generator.getScopes().peek().getStackIndex(variable.getName());
                emitLoad(visitor, variable.getType(), idx);
            }
            case FieldValue field -> {
                loadObjectChain(visitor, field.getSource());
                visitor.visitFieldInsn(Opcodes.GETFIELD,
                        field.getContext().getInternalName(),
                        field.getName(),
                        field.getType().getDescriptor());
            }
            default -> object.codegen(visitor, new Stack<>());
        }

    }

    private void emitLoad(MethodVisitor visitor, Type type, int index) {
        if (type instanceof PrimitiveType primitiveType) {
            switch (primitiveType.getDescriptor()) {
                case "I", "S", "C", "B", "Z" -> visitor.visitVarInsn(Opcodes.ILOAD, index);
                case "J" -> visitor.visitVarInsn(Opcodes.LLOAD, index);
                case "F" -> visitor.visitVarInsn(Opcodes.FLOAD, index);
                case "D" -> visitor.visitVarInsn(Opcodes.DLOAD, index);
                default -> throw new IllegalArgumentException("Unknown primitive type: " + primitiveType.getDescriptor());
            }
        } else {
            visitor.visitVarInsn(Opcodes.ALOAD, index);
        }
    }

    private void emitStore(MethodVisitor visitor, Type type, int index) {
        if (type instanceof PrimitiveType primitiveType) {
            switch (primitiveType.getDescriptor()) {
                case "I", "S", "C", "B", "Z" -> visitor.visitVarInsn(Opcodes.ISTORE, index);
                case "J" -> visitor.visitVarInsn(Opcodes.LSTORE, index);
                case "F" -> visitor.visitVarInsn(Opcodes.FSTORE, index);
                case "D" -> visitor.visitVarInsn(Opcodes.DSTORE, index);
                default -> throw new IllegalArgumentException("Unknown primitive type: " + primitiveType.getDescriptor());
            }
        } else {
            visitor.visitVarInsn(Opcodes.ASTORE, index);
        }
    }

    @Override
    public Type getResult() {
        return switch (operator.getType()) {
            case ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN -> null;

            case EQUALS, NOT_EQUALS, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, IS ->
                    SemanticAnalyzerImpl.getLinkerContext().getResolver()
                            .resolve("boolean").orElse(null);

            case AS -> right instanceof TypeValue typeValue ? typeValue.getValue() : null;

            default -> super.getResult();
        };
    }

}
