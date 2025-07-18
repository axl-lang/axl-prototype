package axl.compiler.analysis.parser.data.expression;

import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import lombok.Data;

import java.util.List;

@Data
public class InvokeExpression implements Expression {

    private Expression source;

    private List<Expression> arguments;

    public InvokeExpression(Expression source, List<Expression> arguments) {
        this.source = source;
        this.arguments = arguments;
    }

    private Object parent;
}
