package axl.compiler.analysis.parser.data.expression;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import lombok.Data;

@Data
public class LiteralExpression implements Expression {

    private Token value;

    public LiteralExpression(Token value) {
        this.value = value;
    }

    private Object parent;
}
