package axl.compiler.analysis.semantic.impl.transform.elt;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Statement;
import axl.compiler.analysis.parser.data.expression.*;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import axl.compiler.analysis.parser.data.statement.*;
import axl.compiler.analysis.semantic.data.tree.DeclarationType;
import axl.compiler.analysis.semantic.data.tree.elt.DeclarationElt;
import axl.compiler.analysis.semantic.data.tree.elt.condition.ConditionElt;
import axl.compiler.analysis.semantic.data.tree.elt.condition.ForeachElt;
import axl.compiler.analysis.semantic.data.tree.elt.condition.WhileElt;
import axl.compiler.analysis.semantic.data.tree.elt.operation.*;
import axl.compiler.analysis.semantic.data.tree.value.*;
import axl.compiler.analysis.semantic.impl.transform.TransformVisitor;
import axl.compiler.linker.type.TypeUtils;
import axl.compiler.linker.type.data.Type;

import java.util.ArrayList;
import java.util.Optional;

public class EltVisitor implements TransformVisitor {

    @Override
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(node instanceof Statement statement) || node instanceof BodyStatement)
            return;

        switch (statement) {
            case TypeReference typeReference -> {
                TypeValue typeValue = new TypeValue();
                typeValue.setValue(resolve(TypeUtils.fromTypeReference(typeReference)).orElseThrow());
                accept(statement, typeValue);
            }
            case AccessExpression accessExpression -> {
                String name = accessExpression.getValue().getValue();
                Optional<Type> type = resolve(name);
                if (type.isEmpty()) {
                    SingleValue value = new SingleValue();
                    value.setValue(accessExpression.getValue());
                    accept(statement, value);
                } else {
                    TypeValue value = new TypeValue();
                    value.setValue(type.get());
                    accept(statement, value);
                }
            }
            case BinaryExpression binaryExpression -> {
                BinaryValue binaryValue = new BinaryValue();
                binaryValue.setOperator(binaryExpression.getOperator());

                binaryExpression.getLeft().setParent(binaryValue);
                treeAnalyzer.enqueue(binaryExpression.getLeft());
                binaryExpression.getRight().setParent(binaryValue);
                treeAnalyzer.enqueue(binaryExpression.getRight());

                accept(statement, binaryValue);
            }
            case UnaryExpression unaryExpression -> {
                UnaryValue unaryValue = new UnaryValue();
                unaryValue.setOperator(unaryExpression.getOperator());

                unaryExpression.getValue().setParent(unaryValue);
                treeAnalyzer.enqueue(unaryExpression.getValue());

                accept(statement, unaryValue);
            }
            case InvokeExpression invokeExpression -> {
                if (invokeExpression.getSource() instanceof AccessExpression accessExpression) {
                    Optional<Type> type = resolve(accessExpression.getValue().getValue());
                    if (type.isPresent()) {
                        NewValue newValue = new NewValue();
                        accept(statement, newValue);

                        newValue.setArguments(new ArrayList<>());
                        newValue.setSource(type.get());
                        for (Expression argument: invokeExpression.getArguments()) {
                            setParent(argument, newValue);
                            treeAnalyzer.enqueue(argument);
                        }
                        break;
                    }
                }

                if (invokeExpression.getSource() instanceof BinaryExpression binaryExpression) {
                    if (binaryExpression.getOperator().getType() == TokenType.DOT) {
                        if (binaryExpression.getLeft() instanceof AccessExpression typeAccessExpression && binaryExpression.getRight() instanceof AccessExpression nonTypeAccessExpression) {
                            Optional<Type> type = resolve(typeAccessExpression.getValue().getValue());
                            if (type.isPresent() && resolve(nonTypeAccessExpression.getValue().getValue()).isEmpty()) {
                                InvokeStaticValue invokeStaticValue = new InvokeStaticValue();
                                accept(statement, invokeStaticValue);

                                invokeStaticValue.setArguments(new ArrayList<>());
                                invokeStaticValue.setSource(type.get());
                                invokeStaticValue.setName(nonTypeAccessExpression.getValue());
                                for (Expression argument: invokeExpression.getArguments()) {
                                    setParent(argument, invokeStaticValue);
                                    treeAnalyzer.enqueue(argument);
                                }
                                break;
                            }
                        }
                    }
                }

                InvokeValue invokeValue = new InvokeValue();
                accept(statement, invokeValue);

                invokeValue.setArguments(new ArrayList<>());

                setParent(invokeExpression.getSource(), invokeValue);
                treeAnalyzer.enqueue(invokeExpression.getSource());

                for (Expression argument: invokeExpression.getArguments()) {
                    setParent(argument, invokeValue);
                    treeAnalyzer.enqueue(argument);
                }
            }
            case LiteralExpression literalExpression -> {
                SingleValue singleValue = new SingleValue();
                singleValue.setValue(literalExpression.getValue());
                accept(statement, singleValue);
            }
            case ForStatement forStatement -> {
                ForeachElt foreachElt = new ForeachElt();
                accept(statement, foreachElt);

                DeclarationElt declarationElt = new DeclarationElt();
                declarationElt.setDeclarationType(DeclarationType.VAR);
                declarationElt.setName(forStatement.getName());
                foreachElt.setDeclaration(declarationElt);

                setParent(forStatement.getBody(), foreachElt);
                treeAnalyzer.enqueue(forStatement.getBody());
                setParent(forStatement.getIterator(), foreachElt);
                treeAnalyzer.enqueue(forStatement.getIterator());
            }
            case IfStatement ifStatement -> {
                ConditionElt conditionElt = new ConditionElt();
                accept(statement, conditionElt);

                setParent(ifStatement.getCondition(), conditionElt);
                treeAnalyzer.enqueue(ifStatement.getCondition());
                setParent(ifStatement.getThen(), conditionElt);
                treeAnalyzer.enqueue(ifStatement.getThen());
                setParent(ifStatement.getThenElse(), conditionElt);
                treeAnalyzer.enqueue(ifStatement.getThenElse());
            }
            case WhileStatement whileStatement -> {
                WhileElt whileElt = new WhileElt();
                accept(statement, whileElt);

                setParent(whileStatement.getCondition(), whileElt);
                treeAnalyzer.enqueue(whileStatement.getCondition());
                setParent(whileStatement.getThen(), whileElt);
                treeAnalyzer.enqueue(whileStatement.getThen());
            }
            case OperationStatement<?> operationStatement -> {
                OperationElt operationElt = null;
                switch (operationStatement.getOperator().getType()) {
                    case BREAK -> {
                        operationElt = new BreakElt();
                    }
                    case CONTINUE -> {
                        operationElt = new ContinueElt();
                    }
                    case RETURN -> {
                        operationElt = new ReturnElt();
                        if (operationStatement.getOperator() instanceof Expression e) {
                            setParent(e, operationElt);
                            treeAnalyzer.enqueue(e);
                        }
                    }
                    case THROW -> {
                        operationElt = new ThrowElt();
                        if (operationStatement.getOperator() instanceof Expression e) {
                            setParent(e, operationElt);
                            treeAnalyzer.enqueue(e);
                        }
                    }
                }
                accept(statement, operationElt);
            }
            case VarStatement varStatement -> {
                DeclarationElt declarationElt = new DeclarationElt();
                accept(statement, declarationElt);

                declarationElt.setDeclarationType(DeclarationType.VAR);
                declarationElt.setName(varStatement.getName());

                if (varStatement.getReference() != null) {
                    declarationElt.setType(resolve(varStatement.getReference()).orElseThrow());
                }

                if (varStatement.getValue() != null) {
                    setParent(varStatement.getValue(), declarationElt);
                    treeAnalyzer.enqueue(varStatement.getValue());
                }
            }
            case ValStatement valStatement -> {
                DeclarationElt declarationElt = new DeclarationElt();
                accept(statement, declarationElt);

                declarationElt.setDeclarationType(DeclarationType.VAL);
                declarationElt.setName(valStatement.getName());

                if (valStatement.getReference() != null) {
                    declarationElt.setType(resolve(valStatement.getReference()).orElseThrow());
                }

                if (valStatement.getValue() != null) {
                    setParent(valStatement.getValue(), declarationElt);
                    treeAnalyzer.enqueue(valStatement.getValue());
                }
            }
            default -> {
            }
        }
    }
}
