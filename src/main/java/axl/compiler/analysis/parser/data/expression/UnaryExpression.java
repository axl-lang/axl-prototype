package axl.compiler.analysis.parser.data.expression;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import lombok.Data;

@Data
public class UnaryExpression implements Expression {

    private Expression value;

    private Token operator;

    private UnaryExpressionType type;

    public UnaryExpression(Expression value, Token operator, UnaryExpressionType type) {
        this.value = value;
        this.operator = operator;
        this.type = type;
    }

    private Object parent;

    public enum UnaryExpressionType {
        PREFIX,
        POSTFIX
    }
}
