package axl.compiler.analysis.semantic.impl.semantic.type.impl;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.data.declaration.ArgumentDeclaration;
import axl.compiler.analysis.semantic.data.scope.Scope;
import axl.compiler.analysis.semantic.data.tree.DeclarationType;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.analysis.semantic.data.tree.elt.DeclarationElt;
import axl.compiler.analysis.semantic.data.tree.elt.FrameElt;
import axl.compiler.analysis.semantic.data.tree.symbol.ClassSymbol;
import axl.compiler.analysis.semantic.data.tree.symbol.FunctionSymbol;
import axl.compiler.analysis.semantic.data.tree.value.*;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.analysis.semantic.impl.semantic.type.TypeVisitor;
import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.type.TypeUtils;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.data.VirtualType;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class TypeVisitorImpl implements TypeVisitor {

    @Override
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (node instanceof FrameElt frameElt) {
            Scope scope;

            if (scopes().isEmpty()) {
                scope = new Scope(null, new HashSet<>());
            } else {
                scope = scopes().peek().createChild();
            }

            if (frameElt.getParent() instanceof FunctionSymbol functionSymbol) {
                if (functionSymbol.getParent() instanceof ClassSymbol classSymbol) {
                    scope.declare("this", classSymbol.getType(), DeclarationType.VAL);
                    scope.initialize("this");
                }

                for (ArgumentDeclaration f : functionSymbol.getDeclaration().getArgumentDeclarations()) {
                    scope.declare(
                            f.getName().getValue(),
                            resolve(TypeUtils.fromTypeReference(f.getReference()))
                                    .orElseThrow(() -> new IllegalStateException("Cannot resolve type for function argument: " + f.getName().getValue())),
                            DeclarationType.VAR
                    );
                    scope.initialize(f.getName().getValue());
                }
            }

            frameElt.setScope(scope);
        }

        TypeVisitor.super.enter(treeAnalyzer, node);
        enqueue(treeAnalyzer, (SemanticNode) node);

        if (!(node instanceof Value value))
            return;

        value.setResult(switch (value) {
            case SingleValue singleValue -> switch (singleValue.getValue().getType()) {
                case IDENTIFY -> {
                    if (singleValue.getParent() instanceof BinaryValue binaryValue) {
                        if (binaryValue.getParent() instanceof InvokeValue) yield null;

                        if (singleValue == binaryValue.getRight() && binaryValue.getOperator().getType() == TokenType.DOT) {
                            Type type = switch (binaryValue.getLeft()) {
                                case TypeValue typeValue -> typeValue.getValue();
                                case Value val -> Objects.requireNonNullElse(val.getResult(), throwRuntime("Left value has no resolved type for static access"));
                            };

                            boolean isStaticAccess = binaryValue.getLeft() instanceof TypeValue;
                            FieldDescriptor field = type.getFields().stream()
                                    .filter(f -> f.getName().equals(singleValue.getValue().getValue())
                                            && ((f.getAccess() & Opcodes.ACC_STATIC) != 0) == isStaticAccess)
                                    .findFirst()
                                    .orElseThrow(() -> new IllegalStateException("Field not found: " + singleValue.getValue().getValue() + " in " + type.getName()));

                            binaryValue.setResult(resolve(field.getType())
                                    .orElseThrow(() -> new IllegalStateException("Cannot resolve type for field: " + field.getName())));

                            yield null;
                        }
                    }

                    if (singleValue.getParent() instanceof InvokeValue invokeValue && invokeValue.getSource() == singleValue) {
                        yield null;
                    }

                    yield getVariableType(singleValue.getValue().getValue());
                }
                case DEC_LONG_NUMBER, HEX_LONG_NUMBER -> resolve("long").orElseThrow(() -> new IllegalStateException("Cannot resolve 'long' type"));
                case DEC_NUMBER, HEX_NUMBER -> resolve("int").orElseThrow(() -> new IllegalStateException("Cannot resolve 'int' type"));
                case DOUBLE_NUMBER -> resolve("double").orElseThrow(() -> new IllegalStateException("Cannot resolve 'double' type"));
                case FLOAT_NUMBER -> resolve("float").orElseThrow(() -> new IllegalStateException("Cannot resolve 'float' type"));
                case CHAR_LITERAL -> resolve("char").orElseThrow(() -> new IllegalStateException("Cannot resolve 'char' type"));
                case STRING_LITERAL -> resolve("java.lang.String").orElseThrow(() -> new IllegalStateException("Cannot resolve 'String' type"));
                case TRUE, FALSE -> resolve("boolean").orElseThrow(() -> new IllegalStateException("Cannot resolve 'boolean' type"));
                default -> throw new IllegalStateException("Unexpected literal type: " + singleValue.getValue().getType());
            };
            default -> null;
        });
    }

    @Override
    public void exit(TreeAnalyzer treeAnalyzer, Object node) {
        if (node instanceof Value value && value.getResult() == null) {
            value.setResult(switch (value) {
                case UnaryValue unaryValue -> unaryValue.getValue().getResult();
                case BinaryValue binaryValue -> switch (binaryValue.getOperator().getType()) {
                    case IS -> {
                        if (!(binaryValue.getRight() instanceof TypeValue))
                            throw new IllegalStateException("'is' operator must have type on right side");
                        yield resolve("boolean").orElseThrow(() -> new IllegalStateException("Cannot resolve 'boolean' type"));
                    }
                    case AS -> {
                        if (!(binaryValue.getRight() instanceof TypeValue typeValue))
                            throw new IllegalStateException("'as' operator must have type on right side");
                        yield typeValue.getValue();
                    }
                    default -> {
                        if (binaryValue.getLeft() instanceof SingleValue sv && sv.getResult() == null) {
                            sv.setResult(scopes().peek().getType(sv.getValue().getValue()));
                        }
                        if (binaryValue.getParent() instanceof InvokeValue invokeValue && invokeValue.getSource() == binaryValue) {
                            if (binaryValue.getRight() instanceof SingleValue right && right.getValue().getType() == TokenType.IDENTIFY) {
                                yield binaryValue.getLeft().getResult();
                            }
                            yield null;
                        }
                        yield enterType(
                                binaryValue.getLeft().getResult(),
                                binaryValue.getRight().getResult(),
                                binaryValue
                        );
                    }
                };
                case NewValue newValue -> newValue.getSource();
                case InvokeValue invokeValue -> {
                    if (invokeValue.getSource() instanceof BinaryValue binaryValue) {
                        if (binaryValue.getOperator().getType() != TokenType.DOT || !(binaryValue.getRight() instanceof SingleValue singleValue))
                            throw new IllegalStateException("Unsupported method call: right must be identifier");

                        invokeValue.setName(singleValue.getValue());

                        invokeValue.setMethod(getRegistry().resolveMethodDeep(
                                (VirtualType) binaryValue.getLeft().getResult(),
                                singleValue.getValue().getValue(),
                                invokeValue.getArguments().stream().map(Value::getResult).map(Object::toString).toList(),
                                false,
                                getContext()
                        ).orElseThrow(() -> new IllegalStateException("Method not found: " + singleValue.getValue().getValue())));

                        invokeValue.setSource(binaryValue.getLeft());
                    } else if (invokeValue.getSource() instanceof SingleValue singleValue) {
                        invokeValue.setName(singleValue.getValue());
                        invokeValue.setMethod(getRegistry().resolveMethodDeep(
                                getContext(),
                                singleValue.getValue().getValue(),
                                invokeValue.getArguments().stream().map(Value::getResult).map(Object::toString).toList(),
                                getContext() == SemanticAnalyzerImpl.getLinkerContext().getContext(),
                                getContext()
                        ).orElseThrow(() -> new IllegalStateException("Static method not found: " + singleValue.getValue().getValue())));
                    } else {
                        throw new IllegalStateException("Unexpected invoke source type: " + invokeValue.getSource().getClass().getSimpleName());
                    }

                    yield resolve(invokeValue.getMethod().method().getReturnType())
                            .orElseThrow(() -> new IllegalStateException("Cannot resolve return type of: " + invokeValue.getName().getValue()));
                }
                case InvokeStaticValue invokeStaticValue -> {
                    invokeStaticValue.setMethod(getRegistry().resolveMethodDeep(
                            (VirtualType) invokeStaticValue.getSource(),
                            invokeStaticValue.getName().getValue(),
                            invokeStaticValue.getArguments().stream().map(Value::getResult).map(Object::toString).toList(),
                            true,
                            getContext()
                    ).orElseThrow(() -> new IllegalStateException("Static method not found: " + invokeStaticValue.getName().getValue())));
                    yield resolve(invokeStaticValue.getMethod().method().getReturnType())
                            .orElseThrow(() -> new IllegalStateException("Cannot resolve return type of static call: " + invokeStaticValue.getName().getValue()));
                }
                case DeclarationElt declarationElt -> {
                    if (declarationElt.getType() != null) yield declarationElt.getType();
                    yield declarationElt.getValue().getResult();
                }
                default -> null;
            });
        }

        if (node instanceof DeclarationElt declarationElt) {
            scopes().peek().declare(
                    declarationElt.getName().getValue(),
                    declarationElt.getType(),
                    declarationElt.getDeclarationType()
            );
        }

        TypeVisitor.super.exit(treeAnalyzer, node);
    }

    private static <T> T throwRuntime(String message) {
        throw new IllegalStateException(message);
    }

    public Type enterType(Type type1, Type type2, BinaryValue binaryValue) {
        if (Objects.equals(type1, type2)) return type1;

        String name1 = type1.getName();
        String name2 = type2.getName();

        int rank1 = getPrimitivePromotionRank(name1);
        int rank2 = getPrimitivePromotionRank(name2);

        if (rank1 != Integer.MAX_VALUE && rank2 != Integer.MAX_VALUE) {
            if (rank1 < rank2) {
                int instruction = getPrimitiveCastInstruction(type1, type2);
                binaryValue.setLeft(createCast(binaryValue.getLeft(), type2, instruction));
                return type2;
            } else if (rank2 < rank1) {
                int instruction = getPrimitiveCastInstruction(type2, type1);
                binaryValue.setRight(createCast(binaryValue.getRight(), type1, instruction));
                return type1;
            }
        }

        if (getRegistry().isAssignable(type1, type2)) return type2;
        if (getRegistry().isAssignable(type2, type1)) return type1;

        throw new RuntimeException("Cannot merge types: " + type1.getName() + " and " + type2.getName());
    }

    private Value createCast(Value value, Type targetType, int instruction) {
        BinaryValue cast = new BinaryValue();
        cast.setLeft(value);
        cast.setOperator(Token.builder().type(TokenType.AS).build());
        cast.setResult(targetType);
        cast.setInstruction(instruction);
        return cast;
    }

    private int getPrimitivePromotionRank(String name) {
        return switch (name) {
            case "byte" -> 1;
            case "short" -> 2;
            case "int" -> 3;
            case "long" -> 4;
            case "float" -> 5;
            case "double" -> 6;
            default -> Integer.MAX_VALUE;
        };
    }

    private int getPrimitiveCastInstruction(Type from, Type to) {
        String fromName = from.getName();
        String toName = to.getName();

        return switch (fromName + "->" + toName) {
            case "int->float", "byte->float", "short->float", "char->float" -> Opcodes.I2F;
            case "int->double" -> Opcodes.I2D;
            case "int->long" -> Opcodes.I2L;
            case "long->float" -> Opcodes.L2F;
            case "long->double" -> Opcodes.L2D;
            case "float->double" -> Opcodes.F2D;
            case "byte->int", "short->int", "char->int" -> Opcodes.NOP;
            default -> throw new IllegalStateException("No cast from " + fromName + " to " + toName);
        };
    }

}
