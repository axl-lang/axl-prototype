package axl.compiler.analysis.parser.data.expression;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AccessExpression implements Expression {

    private Token value;

    public AccessExpression(Token value) {
        this.value = value;
    }

    private Object parent;
}
