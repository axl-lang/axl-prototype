package axl.compiler.analysis.parser.impl.expression;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.expression.*;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import axl.compiler.analysis.parser.exception.ParserException;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser extends Parser<Expression> {

    @Getter
    public static final ExpressionParser instance = new ExpressionParser();

    @Override
    public Expression analyze() {
        return parseExpression(0);
    }

    private Expression parseExpression(int rbp) {
        Token token = tokenStream.next();
        Expression left = nud(token);

        while (tokenStream.hasNext()) {
            Token next = tokenStream.peek();
            if (next == null || getLeftBindingPower(next.getType()) <= rbp) break;
            token = tokenStream.next();
            left = led(left, token);
        }

        return left;
    }

    private Expression nud(Token token) {
        TokenType type = token.getType();

        if (type == TokenType.THIS)
            return new AccessExpression(token);

        return switch (type.getGroup()) {
            case LITERAL -> new LiteralExpression(token);
            case IDENTIFY -> new AccessExpression(token);
            case OPERATOR -> {
                if (isPrefixUnary(type)) {
                    Expression right = parseExpression(getPrefixBindingPower(type));
                    yield new UnaryExpression(right, token, UnaryExpression.UnaryExpressionType.PREFIX);
                }
                throw new ParserException(token.getSection(), "Unexpected operator at expression start: " + type);
            }
            case DELIMITER -> {
                if (type == TokenType.LEFT_PARENT) {
                    if (tokenStream.peek().getType() == TokenType.RIGHT_PARENT) {
                        throw new ParserException(token.getSection(), "Empty grouping parentheses are not allowed");
                    }
                    Expression expr = parseExpression(0);
                    tokenStream.expect(TokenType.RIGHT_PARENT);
                    yield expr;
                }
                throw new ParserException(token.getSection(), "Unexpected delimiter at expression start: " + type);
            }
            default -> throw new ParserException(token.getSection(), "Unexpected token at expression start: " + type);
        };
    }

    private Expression led(Expression left, Token token) {
        TokenType type = token.getType();

        return switch (type.getGroup()) {
            case OPERATOR -> {
                if (isInfixBinary(type)) {
                    int rbp = getLeftBindingPower(type);
                    if (type == TokenType.AS || type == TokenType.IS) {
                        TypeReference right = TypeReferenceParser.getInstance().analyze();
                        yield new BinaryExpression(left, right, token);
                    } else {
                        Expression right = parseExpression(rbp);
                        yield new BinaryExpression(left, right, token);
                    }
                }
                if (isPostfixUnary(type)) {
                    yield new UnaryExpression(left, token, UnaryExpression.UnaryExpressionType.POSTFIX);
                }
                throw new ParserException(token.getSection(), "Unexpected operator in expression: " + type);
            }
            case DELIMITER -> {
                if (type == TokenType.LEFT_PARENT) {
                    List<Expression> args = parseArgumentList();
                    tokenStream.expect(TokenType.RIGHT_PARENT);
                    yield new InvokeExpression(left, args);
                }
                throw new ParserException(token.getSection(), "Unexpected delimiter in expression: " + type);
            }
            default -> throw new ParserException(token.getSection(), "Unexpected token in expression: " + type);
        };
    }

    private List<Expression> parseArgumentList() {
        List<Expression> args = new ArrayList<>();
        if (tokenStream.peek().getType() == TokenType.RIGHT_PARENT) return args;

        while (true) {
            args.add(parseExpression(0));
            if (tokenStream.peek().getType() == TokenType.COMMA) {
                tokenStream.next();
            } else break;
        }

        return args;
    }

    private int getLeftBindingPower(TokenType type) {
        return switch (type) {
            case DOT -> 120;
            case LEFT_PARENT -> 110;
            case MULTIPLY, DIVIDE, MODULO -> 90;
            case PLUS, MINUS -> 80;
            case GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL -> 70;
            case EQUALS, NOT_EQUALS -> 60;
            case AND -> 50;
            case OR -> 40;
            case ASSIGN, MINUS_ASSIGN -> 10;
            case AS, IS -> 5;
            default -> 0;
        };
    }

    private int getPrefixBindingPower(TokenType type) {
        return switch (type) {
            case NOT, UNARY_MINUS, PREFIX_INCREMENT, PREFIX_DECREMENT -> 100;
            default -> 0;
        };
    }

    private boolean isPrefixUnary(TokenType type) {
        return switch (type) {
            case NOT, UNARY_MINUS, PREFIX_INCREMENT, PREFIX_DECREMENT -> true;
            default -> false;
        };
    }

    private boolean isPostfixUnary(TokenType type) {
        return switch (type) {
            case POSTFIX_INCREMENT, POSTFIX_DECREMENT -> true;
            default -> false;
        };
    }

    private boolean isInfixBinary(TokenType type) {
        return switch (type) {
            case DOT, MULTIPLY, DIVIDE, MODULO,
                 PLUS, MINUS,
                 GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL,
                 EQUALS, NOT_EQUALS,
                 AND, OR,
                 ASSIGN, MINUS_ASSIGN, IS, AS -> true;
            default -> false;
        };
    }
}
