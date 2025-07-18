package axl.compiler.analysis.parser.data.expression;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class BinaryExpression implements Expression {

    private Expression left;

    private Expression right;

    private Token operator;

    public BinaryExpression(Expression left, Expression right, Token operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    private Object parent;
}
