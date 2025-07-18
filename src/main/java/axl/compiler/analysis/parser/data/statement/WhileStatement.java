package axl.compiler.analysis.parser.data.statement;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Statement;
import lombok.Data;

@Data
public class WhileStatement implements Statement {

    private Token whileToken;

    private Expression condition;

    private Statement then;

    private Object parent;
}
